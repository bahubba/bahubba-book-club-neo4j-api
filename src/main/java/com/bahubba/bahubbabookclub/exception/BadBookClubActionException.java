package com.bahubba.bahubbabookclub.exception;

/** Custom exception for requests to perform actions that are not possible (e.g. out of date/OBE) */
public class BadBookClubActionException extends RuntimeException {
    public BadBookClubActionException() {
        super("Bad book club action request");
    }

    public BadBookClubActionException(String message) {
        super("Bad book club action request: " + message);
    }
}
