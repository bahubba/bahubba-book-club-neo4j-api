package com.bahubba.bahubbabookclub.exception;

/** Custom exception for unauthorized book club actions */
public class UnauthorizedBookClubActionException extends RuntimeException {
    public UnauthorizedBookClubActionException() {
        super("Unauthorized to perform action on book club");
    }
}
