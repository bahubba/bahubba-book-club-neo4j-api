package com.bahubba.bahubbabookclub.service.impl;

import com.bahubba.bahubbabookclub.exception.*;
import com.bahubba.bahubbabookclub.model.dto.BookClubMembershipDTO;
import com.bahubba.bahubbabookclub.model.entity.BookClub;
import com.bahubba.bahubbabookclub.model.entity.BookClubMembership;
import com.bahubba.bahubbabookclub.model.entity.User;
import com.bahubba.bahubbabookclub.model.enums.BookClubRole;
import com.bahubba.bahubbabookclub.model.mapper.BookClubMapper;
import com.bahubba.bahubbabookclub.model.mapper.BookClubMembershipMapper;
import com.bahubba.bahubbabookclub.model.mapper.UserMapper;
import com.bahubba.bahubbabookclub.model.payload.MembershipCompositeID;
import com.bahubba.bahubbabookclub.model.payload.MembershipUpdate;
import com.bahubba.bahubbabookclub.model.payload.NewOwner;
import com.bahubba.bahubbabookclub.repository.BookClubMembershipRepo;
import com.bahubba.bahubbabookclub.repository.BookClubRepo;
import com.bahubba.bahubbabookclub.service.MembershipService;
import com.bahubba.bahubbabookclub.util.SecurityUtil;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MembershipServiceImpl implements MembershipService {

    private final BookClubMembershipRepo bookClubMembershipRepo;
    private final BookClubRepo bookClubRepo;
    private final BookClubMembershipMapper bookClubMembershipMapper;
    private final BookClubMapper bookClubMapper;
    private final UserMapper userMapper;

    @Override
    public Page<BookClubMembershipDTO> getAll(String bookClubName, int pageNum, int pageSize)
            throws UserNotFoundException, UnauthorizedBookClubActionException, PageSizeTooSmallException,
                    PageSizeTooLargeException {

        // Get the user from the security context
        User user = SecurityUtil.getCurrentUserDetails();
        if (user == null) {
            throw new UserNotFoundException();
        }

        // Get the User's membership in the book club, ensuring they are an admin
        bookClubMembershipRepo
                .findByBookClubNameAndClubRoleAndUserId(bookClubName, BookClubRole.ADMIN, user.getId())
                .orElseThrow(UnauthorizedBookClubActionException::new);

        // Ensure the page size is valid
        if (pageSize < 1) {
            throw new PageSizeTooSmallException(10, getPageOfMembershipsForBookClub(bookClubName, pageNum, 10));
        } else if (pageSize > 50) {
            throw new PageSizeTooLargeException(50, 50, getPageOfMembershipsForBookClub(bookClubName, pageNum, 50));
        }

        // Get all members of the book club using the given page size
        return getPageOfMembershipsForBookClub(bookClubName, pageNum, pageSize);
    }

    @Override
    public BookClubRole getRole(String bookClubName) throws UserNotFoundException, MembershipNotFoundException {
        // Get the current user from the security context
        User user = SecurityUtil.getCurrentUserDetails();
        if (user == null) {
            throw new UserNotFoundException();
        }

        // Get the user's role in the book club (if any)
        BookClubMembership membership = bookClubMembershipRepo
                .findByBookClubNameAndUserId(bookClubName, user.getId())
                .orElseThrow(() -> new MembershipNotFoundException(user.getUsername(), bookClubName));

        // Return the user's role
        return membership.getClubRole();
    }

    @Override
    public BookClubMembershipDTO getMembership(String bookClubName)
            throws UserNotFoundException, BookClubNotFoundException {

        // Get the current user from the security context
        User user = SecurityUtil.getCurrentUserDetails();
        if (user == null) {
            throw new UserNotFoundException();
        }

        // Get the user's membership in the book club (if any)
        BookClubMembership membership = bookClubMembershipRepo
                .findByBookClubNameAndUserId(bookClubName, user.getId())
                .orElse(null);

        // If there is no membership, create a transient one with the user and no role
        if (membership == null) {
            BookClub bookClub = bookClubRepo
                    .findByName(bookClubName)
                    .orElseThrow(() -> new BookClubNotFoundException(bookClubName));

            return BookClubMembershipDTO.builder()
                    .bookClub(bookClubMapper.entityToDTO(bookClub))
                    .user(userMapper.entityToDTO(user))
                    .clubRole(BookClubRole.NONE)
                    .isOwner(false)
                    .build();
        }

        // Otherwise return the membership
        return bookClubMembershipMapper.entityToDTO(membership);
    }

    @Override
    public BookClubMembershipDTO updateMembership(MembershipUpdate membershipUpdate)
            throws UserNotFoundException, BadBookClubActionException, UnauthorizedBookClubActionException,
                    MembershipNotFoundException {
        // Get the current user from the security context
        User user = SecurityUtil.getCurrentUserDetails();
        if (user == null) {
            throw new UserNotFoundException();
        }

        // Ensure the user is not trying to update their own role
        if (user.getId().equals(membershipUpdate.getUserID())) {
            throw new BadBookClubActionException();
        }

        // Get the requesting user's membership in the book club to ensure they're an admin
        bookClubMembershipRepo
                .findByBookClubNameAndUserIdAndClubRoleAndDepartedIsNull(
                        membershipUpdate.getBookClubName(), user.getId(), BookClubRole.ADMIN)
                .orElseThrow(UnauthorizedBookClubActionException::new);

        // Get the target user's membership in the book club
        BookClubMembership membership = bookClubMembershipRepo
                .findByBookClubNameAndUserIdAndDepartedIsNull(
                        membershipUpdate.getBookClubName(), membershipUpdate.getUserID())
                .orElseThrow(() -> new MembershipNotFoundException(
                        membershipUpdate.getUserID(), membershipUpdate.getBookClubName()));

        // Ensure the target user is not the owner of the book club, and we're not trying to change to
        // the same role
        if (membership.isOwner() || membership.getClubRole() == membershipUpdate.getRole()) {
            throw new BadBookClubActionException();
        }

        // Update the user's role
        membership.setClubRole(membershipUpdate.getRole());
        return bookClubMembershipMapper.entityToDTO(bookClubMembershipRepo.save(membership));
    }

    @Override
    public BookClubMembershipDTO deleteMembership(String bookClubName, UUID userID)
            throws UserNotFoundException, BadBookClubActionException, UnauthorizedBookClubActionException,
                    MembershipNotFoundException {

        // Get the current user from the security context
        User user = SecurityUtil.getCurrentUserDetails();
        if (user == null) {
            throw new UserNotFoundException("Not logged in or user not found");
        }

        // Ensure the user is not trying to delete their own membership
        if (user.getId().equals(userID)) {
            throw new BadBookClubActionException();
        }

        // Get the requesting user's membership in the book club to ensure they're an admin
        bookClubMembershipRepo
                .findByBookClubNameAndUserIdAndClubRoleAndDepartedIsNull(bookClubName, user.getId(), BookClubRole.ADMIN)
                .orElseThrow(UnauthorizedBookClubActionException::new);

        // Get the target user's membership in the book club
        BookClubMembership membership = bookClubMembershipRepo
                .findByBookClubNameAndUserIdAndDepartedIsNull(bookClubName, userID)
                .orElseThrow(() -> new MembershipNotFoundException(userID, bookClubName));

        // Ensure the target user is not the owner of the book club
        if (membership.isOwner()) {
            throw new BadBookClubActionException();
        }

        // Delete the membership
        membership.setDeparted(LocalDateTime.now());
        return bookClubMembershipMapper.entityToDTO(bookClubMembershipRepo.save(membership));
    }

    @Override
    public Boolean addOwner(NewOwner newOwner)
            throws UserNotFoundException, BadBookClubActionException, UnauthorizedBookClubActionException,
                    MembershipNotFoundException {
        // Get the current user from the security context
        User user = SecurityUtil.getCurrentUserDetails();
        if (user == null) {
            throw new UserNotFoundException("Not logged in or user not found");
        }

        // Ensure the owner isn't trying to change ownership to themselves (the existing owner)
        if (user.getId().equals(newOwner.getNewOwnerID())) {
            throw new BadBookClubActionException();
        }

        // Ensure the user is not trying to change ownership of a book club they don't own
        bookClubMembershipRepo
                .findByBookClubNameAndUserIdAndIsOwnerTrue(newOwner.getBookClubName(), user.getId())
                .orElseThrow(UnauthorizedBookClubActionException::new);

        // Get the new owner's membership
        BookClubMembership newOwnerMembership = bookClubMembershipRepo
                .findByBookClubNameAndUserIdAndDepartedIsNull(newOwner.getBookClubName(), newOwner.getNewOwnerID())
                .orElseThrow(
                        () -> new MembershipNotFoundException(newOwner.getNewOwnerID(), newOwner.getBookClubName()));

        // Change ownership (and make the new owner an admin in case they weren't already)
        newOwnerMembership.setClubRole(BookClubRole.ADMIN);
        newOwnerMembership.setOwner(true);
        bookClubMembershipRepo.save(newOwnerMembership);

        return true;
    }

    @Override
    public BookClubMembershipDTO revokeOwnership(MembershipCompositeID membershipCompositeID)
            throws UserNotFoundException, BadBookClubActionException, UnauthorizedBookClubActionException,
                    MembershipNotFoundException {
        // Get the current user from the security context
        User user = SecurityUtil.getCurrentUserDetails();
        if (user == null) {
            throw new UserNotFoundException("Not logged in or user not found");
        }

        // Ensure the user is not trying to revoke their own membership
        if (user.getId().equals(membershipCompositeID.getUserID())) {
            throw new BadBookClubActionException("Cannot revoke your own membership");
        }

        // Get the memberships of the user and the target user
        List<BookClubMembership> memberships =
                bookClubMembershipRepo.findAllByBookClubIdAndIsOwnerTrueAndDepartedIsNullAndUserIdIn(
                        membershipCompositeID.getBookClubID(),
                        List.of(user.getId(), membershipCompositeID.getUserID()));

        // Ensure the user is an active owner of the book club
        memberships.stream()
                .filter(membership -> membership.getUser().getId().equals(user.getId()))
                .findFirst()
                .orElseThrow(UnauthorizedBookClubActionException::new);

        // Ensure the target user is an active owner of the book club
        BookClubMembership targetMembership = memberships.stream()
                .filter(membership -> membership.getUser().getId().equals(membershipCompositeID.getUserID()))
                .findFirst()
                .orElseThrow(() -> new MembershipNotFoundException(
                        membershipCompositeID.getUserID(), membershipCompositeID.getBookClubID()));

        // Set the target user's ownership to false and persist it to the DB, then return the updated membership
        targetMembership.setOwner(false);
        return bookClubMembershipMapper.entityToDTO(bookClubMembershipRepo.save(targetMembership));
    }

    /**
     * Get a page of memberships for a book club
     *
     * @param bookClubName The name of the book club
     * @param pageNum The page number to retrieve
     * @param pageSize The number of results per page
     * @return A page of memberships for the book club
     */
    private @NotNull Page<BookClubMembershipDTO> getPageOfMembershipsForBookClub(
            String bookClubName, int pageNum, int pageSize) {
        // Get results
        Page<BookClubMembership> entityPage = bookClubMembershipRepo.findAllByBookClubNameOrderByJoined(
                bookClubName, PageRequest.of(pageNum, pageSize));

        // Convert results to DTOs and return
        return entityPage.map(bookClubMembershipMapper::entityToDTO);
    }
}
