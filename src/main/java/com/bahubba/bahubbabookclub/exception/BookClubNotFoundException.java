package com.bahubba.bahubbabookclub.exception;

import java.util.UUID;

/**
 * Custom exception for when a client searches for a book club that doesn't exist (in an active
 * state)
 */
public class BookClubNotFoundException extends RuntimeException {

    /**
     * Generates exception for missing book club by name
     *
     * @param name book club name
     */
    public BookClubNotFoundException(String name) {
        super("Book Club could not be found with name '" + name + "'");
    }

    /**
     * Generates exception for missing book club by ID
     *
     * @param id book club ID
     */
    public BookClubNotFoundException(UUID id) {
        super("Book Club could not be found with ID '" + id + "'");
    }
}
