package com.bahubba.bahubbabookclub.exception;

import java.io.Serial;
import java.io.Serializable;

/** Custom exception for issues when creating a refresh auth token */
public class TokenRefreshException extends RuntimeException implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Constructor
     *
     * @param token refresh token
     * @param message reason the refresh token action failed
     */
    public TokenRefreshException(String token, String message) {
        super(String.format("Failed for [%s]: %s", token, message));
    }
}
