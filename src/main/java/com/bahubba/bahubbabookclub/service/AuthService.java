package com.bahubba.bahubbabookclub.service;

import com.bahubba.bahubbabookclub.exception.UserNotFoundException;
import com.bahubba.bahubbabookclub.model.dto.AuthDTO;
import com.bahubba.bahubbabookclub.model.payload.AuthRequest;
import com.bahubba.bahubbabookclub.model.payload.UserPayload;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.AuthenticationException;

/** Registration and authentication logic */
public interface AuthService {

    /**
     * Registers a user
     *
     * @param newUser New user information
     * @return Persisted user information
     * @throws UserNotFoundException The user's info wasn't persisted to the DB for creating the
     *     auth tokens
     */
    AuthDTO register(UserPayload newUser) throws UserNotFoundException;

    /**
     * Accepts user credentials and returns auth and refresh JWTs in HTTP-Only cookies
     *
     * @param req User credentials (username and password)
     * @return The user's stored info and JWTs
     * @throws AuthenticationException The credentials were invalid
     * @throws UserNotFoundException The user was not found in the DB to delete existing refresh
     *     tokens
     */
    AuthDTO authenticate(@NotNull AuthRequest req) throws AuthenticationException, UserNotFoundException;

    /**
     * Logs out the user by deleting the auth and refresh cookies
     *
     * @param req The request containing the cookies
     * @return An empty AuthDTO
     */
    AuthDTO logout(HttpServletRequest req);
}
