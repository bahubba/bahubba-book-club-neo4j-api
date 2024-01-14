package com.bahubba.bahubbabookclub.exception;

import java.util.UUID;

/**
 * Custom exception for when a client searches for a book club that doesn't exist (in an active
 * state)
 */
public class MembershipNotFoundException extends RuntimeException {

    /**
     * Generates exception for missing book club membership by user ID and book club ID
     *
     * @param userId user ID
     * @param bookClubId book club ID
     */
    public MembershipNotFoundException(UUID userId, UUID bookClubId) {
        super("User with ID '" + userId + "' does not have a membership in book club with ID '" + bookClubId + "'");
    }

    /**
     * Generates exception for missing book club membership by user username and book club name
     *
     * @param username user username
     * @param bookClubName book club name
     */
    public MembershipNotFoundException(String username, String bookClubName) {
        super("User '" + username + "' does not have a membership in book club '" + bookClubName + "'");
    }

    /**
     * Generates exception for missing book club membership by user ID and book club name
     *
     * @param userID user ID
     * @param bookClubName book club name
     */
    public MembershipNotFoundException(UUID userID, String bookClubName) {
        super("User with ID '" + userID + "' does not have a membership in '" + bookClubName + "'");
    }
}
