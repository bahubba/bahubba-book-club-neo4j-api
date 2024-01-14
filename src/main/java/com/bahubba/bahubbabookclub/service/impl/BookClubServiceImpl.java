package com.bahubba.bahubbabookclub.service.impl;

import com.bahubba.bahubbabookclub.exception.*;
import com.bahubba.bahubbabookclub.model.dto.BookClubDTO;
import com.bahubba.bahubbabookclub.model.dto.S3ImageDTO;
import com.bahubba.bahubbabookclub.model.entity.BookClub;
import com.bahubba.bahubbabookclub.model.entity.BookClubMembership;
import com.bahubba.bahubbabookclub.model.entity.Notification;
import com.bahubba.bahubbabookclub.model.entity.User;
import com.bahubba.bahubbabookclub.model.enums.BookClubRole;
import com.bahubba.bahubbabookclub.model.enums.NotificationType;
import com.bahubba.bahubbabookclub.model.enums.Publicity;
import com.bahubba.bahubbabookclub.model.mapper.BookClubMapper;
import com.bahubba.bahubbabookclub.model.payload.BookClubPayload;
import com.bahubba.bahubbabookclub.repository.BookClubMembershipRepo;
import com.bahubba.bahubbabookclub.repository.BookClubRepo;
import com.bahubba.bahubbabookclub.repository.NotificationRepo;
import com.bahubba.bahubbabookclub.service.BookClubService;
import com.bahubba.bahubbabookclub.service.S3Service;
import com.bahubba.bahubbabookclub.util.APIConstants;
import com.bahubba.bahubbabookclub.util.SecurityUtil;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.model.S3Object;

/** {@link BookClub} business logic implementation */
@Service
@Transactional
@RequiredArgsConstructor
public class BookClubServiceImpl implements BookClubService {

    private final S3Service s3Service;
    private final BookClubRepo bookClubRepo;
    private final BookClubMembershipRepo bookClubMembershipRepo;
    private final NotificationRepo notificationRepo;
    private final BookClubMapper bookClubMapper;

    @Override
    public BookClubDTO create(BookClubPayload newBookClub) throws UserNotFoundException, BadBookClubActionException {
        // Get the current user from the security context
        User user = SecurityUtil.getCurrentUserDetails();
        if (user == null) {
            throw new UserNotFoundException();
        }

        // Ensure the book club's name is not a reserved word
        if (Arrays.stream(APIConstants.RESERVED_NAMES).anyMatch(newBookClub.getName()::equalsIgnoreCase)) {
            throw new BadBookClubActionException();
        }

        // Convert the book club to an entity and persist it
        BookClub newBookClubEntity = bookClubRepo.save(bookClubMapper.payloadToEntity(newBookClub));

        // Add the user as a member/owner
        bookClubMembershipRepo.save(BookClubMembership.builder()
                .bookClub(newBookClubEntity)
                .user(user)
                .clubRole(BookClubRole.ADMIN)
                .isOwner(true)
                .build());

        // Generate a notification for the book club's creation
        notificationRepo.save(Notification.builder()
                .sourceUser(user)
                .targetUser(user)
                .bookClub(newBookClubEntity)
                .type(NotificationType.BOOK_CLUB_CREATED)
                .build());

        return bookClubMapper.entityToDTO(newBookClubEntity);
    }

    @Override
    public BookClubDTO update(BookClubPayload updatedBookClub)
            throws UserNotFoundException, UnauthorizedBookClubActionException {
        // Get the current user from the security context
        User user = SecurityUtil.getCurrentUserDetails();
        if (user == null) {
            throw new UserNotFoundException();
        }

        // Find the book club to update
        BookClub bookClub = bookClubRepo
                .findByIdAndUserIsAdmin(updatedBookClub.getId(), user.getId())
                .orElseThrow(() -> new BookClubNotFoundException(updatedBookClub.getId()));

        // Update the book club's metadata
        bookClub.setName(updatedBookClub.getName());
        bookClub.setDescription(updatedBookClub.getDescription());
        bookClub.setImageFileName(updatedBookClub.getImageFileName());
        bookClub.setPublicity(updatedBookClub.getPublicity());

        // TODO - Add notifications for each piece of metadata that was updated

        // Persist and return the book club
        return bookClubMapper.entityToDTO(bookClubRepo.save(bookClub));
    }

    @Override
    public BookClubDTO findByID(UUID id)
            throws BookClubNotFoundException, UserNotFoundException, MembershipNotFoundException {

        // Get the book club by ID
        BookClub bookClub = bookClubRepo.findById(id).orElseThrow(() -> new BookClubNotFoundException(id));

        // If the book club is not private, return it
        if (bookClub.getPublicity() != Publicity.PRIVATE) {
            return bookClubMapper.entityToDTO(bookClub);
        }

        // Otherwise, check if the current user is a member of the book club
        return checkBookClubMembership(bookClub);
    }

    @Override
    public BookClubDTO findByName(String name)
            throws BookClubNotFoundException, UserNotFoundException, MembershipNotFoundException {
        // Get the book club by name
        BookClub bookClub = bookClubRepo.findByName(name).orElseThrow(() -> new BookClubNotFoundException(name));

        // If the book club is not private, return it
        if (bookClub.getPublicity() != Publicity.PRIVATE) {
            return bookClubMapper.entityToDTO(bookClub);
        }

        // Otherwise, check if the current user is a member of the book club
        return checkBookClubMembership(bookClub);
    }

    @Override
    public Page<BookClubDTO> findAllForUser(int pageNum, int pageSize)
            throws UserNotFoundException, PageSizeTooSmallException, PageSizeTooLargeException {

        // Get the current user from the security context
        User user = SecurityUtil.getCurrentUserDetails();
        if (user == null) {
            throw new UserNotFoundException();
        }

        // Ensure the page size is appropriate
        if (pageSize < 0) {
            throw new PageSizeTooSmallException(10, getPageOfAllForUser(user.getId(), pageNum, 10));
        } else if (pageSize > 50) {
            throw new PageSizeTooLargeException(50, 50, getPageOfAllForUser(user.getId(), pageNum, 50));
        }

        return getPageOfAllForUser(user.getId(), pageNum, pageSize);
    }

    @Override
    public Page<BookClubDTO> findAll(int pageNum, int pageSize)
            throws PageSizeTooSmallException, PageSizeTooLargeException {

        // Ensure the page size is appropriate
        if (pageSize < 0) {
            // If the page size is negative, throw an error, but default the page size to 10 and return
            // results
            throw new PageSizeTooSmallException(10, getPageOfAll(pageNum, 10));
        } else if (pageSize > 50) {
            // If the page size is > 50, throw an error, but default the page size to 50 and return
            // results
            throw new PageSizeTooLargeException(50, 50, getPageOfAll(pageNum, 50));
        }

        // Get results using the appropriate page size
        return getPageOfAll(pageNum, pageSize);
    }

    @Override
    public Page<BookClubDTO> search(String searchTerm, int pageNum, int pageSize)
            throws PageSizeTooSmallException, PageSizeTooLargeException {

        // Ensure the page size is appropriate
        if (pageSize < 1) {
            // If the page size is negative, throw an error, but default the page size to 10 and return
            // results
            throw new PageSizeTooSmallException(10, getPageOfSearchResults(searchTerm, pageNum, 10));
        } else if (pageSize > 50) {
            // If the page size is > 50, throw an error, but default the page size to 50 and return
            // results
            throw new PageSizeTooLargeException(50, 50, getPageOfSearchResults(searchTerm, pageNum, 50));
        }

        // Get results using the appropriate page size
        return getPageOfSearchResults(searchTerm, pageNum, pageSize);
    }

    @Override
    public BookClubDTO disbandBookClubByID(UUID id)
            throws UserNotFoundException, MembershipNotFoundException, UnauthorizedBookClubActionException,
                    BadBookClubActionException {

        // Get the current user from the security context
        User user = SecurityUtil.getCurrentUserDetails();
        if (user == null) {
            throw new UserNotFoundException();
        }

        // Find the user's membership in the book club
        BookClubMembership membership = bookClubMembershipRepo
                .findByBookClubIdAndUserId(id, user.getId())
                .orElseThrow(() -> new MembershipNotFoundException(user.getId(), id));

        return disbandBookClub(membership);
    }

    @Override
    public BookClubDTO disbandBookClubByName(String name)
            throws UserNotFoundException, MembershipNotFoundException, UnauthorizedBookClubActionException,
                    BadBookClubActionException {

        // Get the current user from the security context
        User user = SecurityUtil.getCurrentUserDetails();
        if (user == null) {
            throw new UserNotFoundException();
        }

        // Find the user's membership in the book club
        BookClubMembership membership = bookClubMembershipRepo
                .findByBookClubNameAndUserId(name, user.getId())
                .orElseThrow(() -> new MembershipNotFoundException(user.getUsername(), name));

        return disbandBookClub(membership);
    }

    @Override
    public List<S3ImageDTO> getStockBookClubImages() {
        List<S3ImageDTO> stockImages = new ArrayList<>();

        // Get the list of stock book club images from S3
        List<S3Object> s3ImageObjects = s3Service.listS3ObjectsAtPrefix(APIConstants.BOOK_CLUB_STOCK_IMAGE_PREFIX);

        // Get the pre-signed URLs for each image
        for (S3Object s3ImageObject : s3ImageObjects) {
            if (s3ImageObject.size() > 0) {
                stockImages.add(S3ImageDTO.builder()
                        .fileName(s3ImageObject
                                .key()
                                .substring(s3ImageObject.key().lastIndexOf("/") + 1))
                        .url(s3Service.getPreSignedURL(s3ImageObject.key()))
                        .build());
            }
        }

        return stockImages;
    }

    /**
     * Ensure a user is a member of a book club before returning the book club
     *
     * @param bookClub The book club to check
     * @return The book club if the user is a member
     * @throws UserNotFoundException The user was not logged in
     * @throws MembershipNotFoundException The user was not a member of the book club
     */
    private BookClubDTO checkBookClubMembership(BookClub bookClub)
            throws UserNotFoundException, MembershipNotFoundException {

        // Get the current user from the security context
        User user = SecurityUtil.getCurrentUserDetails();
        if (user == null) {
            throw new UserNotFoundException();
        }

        // Check if the user is a member of the book club
        if (Boolean.FALSE.equals(bookClubMembershipRepo.existsByBookClubIdAndUserId(bookClub.getId(), user.getId()))) {
            throw new MembershipNotFoundException(user.getUsername(), bookClub.getName());
        }

        return bookClubMapper.entityToDTO(bookClub);
    }

    /**
     * Retrieves a page of book clubs that the user has some role in
     *
     * @param userID The UUID of the user
     * @param pageNum The page number
     * @param pageSize The number of results per page
     */
    private @NotNull Page<BookClubDTO> getPageOfAllForUser(UUID userID, int pageNum, int pageSize) {
        // Get results
        Page<BookClub> entityPage = bookClubRepo.findAllForUser(userID, PageRequest.of(pageNum, pageSize));

        // Convert results to DTOs and return
        return entityPage.map(bookClubMapper::entityToDTO);
    }

    /**
     * Retrieves a page of all book clubs
     *
     * @param pageNum The page number
     * @param pageSize The number of results per page
     */
    private @NotNull Page<BookClubDTO> getPageOfAll(int pageNum, int pageSize) {
        // Get results
        Page<BookClub> entityPage = bookClubRepo.findAll(PageRequest.of(pageNum, pageSize));

        // Convert results to DTOs and return
        return entityPage.map(bookClubMapper::entityToDTO);
    }

    /**
     * Searches for book clubs by name, returning a paged subset
     *
     * @param searchTerm the substring of the name to search for
     * @param pageNum the page number
     * @param pageSize the size of the page
     */
    private @NotNull Page<BookClubDTO> getPageOfSearchResults(String searchTerm, int pageNum, int pageSize) {
        // Get results
        Page<BookClub> entityPage = bookClubRepo.findAllByPublicityNotAndNameContainsIgnoreCase(
                Publicity.PRIVATE, searchTerm, PageRequest.of(pageNum, pageSize));

        // Convert results to DTOs and return
        return entityPage.map(bookClubMapper::entityToDTO);
    }

    /**
     * Disband a book club
     *
     * @param membership The membership of the user in the book club to disband
     * @return The disbanded book club
     * @throws UnauthorizedBookClubActionException The user was not the owner of the book club
     * @throws BadBookClubActionException The book club was already disbanded
     */
    private BookClubDTO disbandBookClub(@NotNull BookClubMembership membership)
            throws UnauthorizedBookClubActionException, BadBookClubActionException {
        // Ensure the user is the owner of the book club
        if (!membership.isOwner()) {
            throw new UnauthorizedBookClubActionException();
        }

        // Ensure the book club is not already disbanded
        BookClub bookClub = membership.getBookClub();
        if (bookClub.getDisbanded() != null) {
            throw new BadBookClubActionException();
        }

        // Disband the book club
        bookClub.setDisbanded(LocalDateTime.now());
        return bookClubMapper.entityToDTO(bookClubRepo.save(bookClub));
    }
}
