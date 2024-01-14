package com.bahubba.bahubbabookclub.exception;

import com.bahubba.bahubbabookclub.util.APIConstants;
import java.util.UUID;

/**
 * Custom exception for when a client searches for a user that doesn't exist (in an active
 * state)
 */
public class UserNotFoundException extends RuntimeException {

    /** Generates exception for missing user in security context */
    public UserNotFoundException() {
        super(APIConstants.USER_NOT_FOUND);
    }

    /**
     * Generates exception for missing user by username or email
     *
     * @param usernameOrEmail user username or email
     */
    public UserNotFoundException(String usernameOrEmail) {
        super("User could not be found with username or email matching '" + usernameOrEmail + "'");
    }

    /**
     * Generates exception for missing user by username or email
     *
     * @param id user ID
     */
    public UserNotFoundException(UUID id) {
        super("User could not be found with ID '" + id + "'");
    }

    /** Generates an exception for a user not being found in a book club */
    public UserNotFoundException(String username, String bookClubName) {
        super("User '" + username + "' not found in book club '" + bookClubName + "'");
    }
}
