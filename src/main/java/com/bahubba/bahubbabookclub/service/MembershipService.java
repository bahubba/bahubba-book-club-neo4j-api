package com.bahubba.bahubbabookclub.service;

import com.bahubba.bahubbabookclub.exception.*;
import com.bahubba.bahubbabookclub.model.dto.BookClubMembershipDTO;
import com.bahubba.bahubbabookclub.model.enums.BookClubRole;
import com.bahubba.bahubbabookclub.model.payload.MembershipCompositeID;
import com.bahubba.bahubbabookclub.model.payload.MembershipUpdate;
import com.bahubba.bahubbabookclub.model.payload.NewOwner;
import java.util.UUID;
import org.springframework.data.domain.Page;

public interface MembershipService {

    /**
     * Get all members of a book club
     *
     * @param bookClubName The name of a book club
     * @param pageNum The page number to retrieve
     * @param pageSize The number of results per page
     * @return A list of all members of the book club
     * @throws UserNotFoundException The user was not logged in or did not exist
     * @throws UnauthorizedBookClubActionException The user was not an admin of the book club
     * @throws PageSizeTooSmallException The page size was < 1
     * @throws PageSizeTooLargeException The page size was > 50
     */
    Page<BookClubMembershipDTO> getAll(String bookClubName, int pageNum, int pageSize)
            throws UserNotFoundException, UnauthorizedBookClubActionException, PageSizeTooSmallException,
                    PageSizeTooLargeException;

    /**
     * Get the role of a user in a book club
     *
     * @param bookClubName The name of the book club
     * @return The user's role in the book club
     * @throws UserNotFoundException The user was not logged in or did not exist
     * @throws MembershipNotFoundException The user was not a member of the book club
     */
    BookClubRole getRole(String bookClubName) throws UserNotFoundException, MembershipNotFoundException;

    /**
     * Get a user's membership in a book club
     *
     * @param bookClubName The name of the book club
     * @return The user's membership info
     * @throws UserNotFoundException The user was not logged in or did not exist
     * @throws BookClubNotFoundException The book club did not exist
     */
    BookClubMembershipDTO getMembership(String bookClubName) throws UserNotFoundException, BookClubNotFoundException;

    /**
     * Update a user's role in a book club
     *
     * @param membershipUpdate The book club and user to update
     * @return The updated membership
     * @throws UserNotFoundException The user was not logged in or did not exist
     * @throws BadBookClubActionException The user attempted to update their own role, the target
     *     user was the owner of the book club, or there was no real change requested
     * @throws UnauthorizedBookClubActionException The user was not an admin of the book club
     * @throws MembershipNotFoundException The target user was not a member of the book club
     */
    BookClubMembershipDTO updateMembership(MembershipUpdate membershipUpdate)
            throws UserNotFoundException, BadBookClubActionException, UnauthorizedBookClubActionException,
                    MembershipNotFoundException;

    /**
     * Delete a user's membership in a book club
     *
     * @param bookClubName The name of the book club
     * @param userID The ID of the user
     * @return The deleted membership
     * @throws UserNotFoundException The user was not logged in or did not exist
     * @throws BadBookClubActionException The user attempted to delete their own membership or the
     *     target user was the owner of the book club
     * @throws UnauthorizedBookClubActionException The user was not an admin of the book club
     * @throws MembershipNotFoundException The target user was not a member of the book club
     */
    BookClubMembershipDTO deleteMembership(String bookClubName, UUID userID)
            throws UserNotFoundException, BadBookClubActionException, UnauthorizedBookClubActionException,
                    MembershipNotFoundException;

    /**
     * Change ownership of a book club
     *
     * @param newOwner The book club and new owner ID
     * @return true if successful
     * @throws UserNotFoundException The user was not logged in or did not exist
     * @throws BadBookClubActionException The user is trying to make themselves the owner
     * @throws UnauthorizedBookClubActionException The user was not the existing owner of the book
     *     club
     * @throws MembershipNotFoundException The target user was not a member of the book club
     */
    Boolean addOwner(NewOwner newOwner)
            throws UserNotFoundException, BadBookClubActionException, UnauthorizedBookClubActionException,
                    MembershipNotFoundException;

    /**
     * Revoke a user's ownership of a book club
     *
     * @param membershipCompositeID The IDs of the book club and the user to revoke ownership from
     * @return An updated version of the membership
     * @throws UserNotFoundException The user was not logged in or did not exist
     * @throws BadBookClubActionException The user is trying to revoke their own ownership, or the target user was not an active owner
     * @throws UnauthorizedBookClubActionException The user was not an owner of the book
     * @throws MembershipNotFoundException The target user was not a member of the book club
     */
    BookClubMembershipDTO revokeOwnership(MembershipCompositeID membershipCompositeID)
            throws UserNotFoundException, BadBookClubActionException, UnauthorizedBookClubActionException,
                    MembershipNotFoundException;
}
