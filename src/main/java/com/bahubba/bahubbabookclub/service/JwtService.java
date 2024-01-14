package com.bahubba.bahubbabookclub.service;

import com.bahubba.bahubbabookclub.exception.TokenRefreshException;
import com.bahubba.bahubbabookclub.exception.UserNotFoundException;
import com.bahubba.bahubbabookclub.model.dto.AuthDTO;
import com.bahubba.bahubbabookclub.model.entity.RefreshToken;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;

/** JWT auth token service layer */
public interface JwtService {
    /**
     * Generates a JWT in a response cookie for authentication
     *
     * @param userDetails The user's credentials and info
     * @return A response cookie with the JWT for authentication
     */
    ResponseCookie generateJwtCookie(UserDetails userDetails);

    /**
     * Generates a JWT in a response cookie for refreshing the auth token
     *
     * @param refreshToken The string version of the refresh token
     * @return A response cookie with the JWT for refreshing the auth token
     */
    ResponseCookie generateJwtRefreshCookie(String refreshToken);

    /**
     * Pulls the JWT auth token from the request cookies
     *
     * @param req The incoming HTTP request
     * @return A string version of the JWT auth token
     */
    String getJwtFromCookies(HttpServletRequest req);

    /**
     * Pulls the JWT refresh token from the request cookies
     *
     * @param req The incoming HTTP request
     * @return A string version of the JWT refresh token
     */
    String getJwtRefreshFromCookies(HttpServletRequest req);

    /**
     * Pulls the username from the JWT auth token
     *
     * @param token The JWT auth token
     * @return The username
     */
    String extractUsername(String token);

    /**
     * Verifies that the token is for the requesting user and is not expired
     *
     * @param token The JWT auth token
     * @param userDetails The user's credentials and info
     * @return Whether the token is valid
     */
    boolean isTokenValid(String token, UserDetails userDetails);

    /**
     * Pulls a claim from the JWT auth token
     *
     * @param <T> The type of the claim to be pulled
     * @param token The JWT auth token
     * @param claimsResolver Function used to pull the claim
     * @return A claim from the JWT auth token
     */
    <T> T extractClaim(String token, Function<Claims, T> claimsResolver);

    /**
     * Generates a new JWT auth token given a valid refresh token
     *
     * @param req The incoming HTTP request
     * @return A new auth object with the user's info and auth and refresh JWTs
     * @throws TokenRefreshException The refresh token was missing or expired
     */
    AuthDTO refreshToken(HttpServletRequest req) throws TokenRefreshException;

    /**
     * Generates a new JWT auth token given a valid refresh token
     *
     * @param token The refresh token
     * @return A new auth object with the user's info and auth and refresh JWTs
     * @throws TokenRefreshException The refresh token was missing or expired
     */
    AuthDTO refreshToken(String token) throws TokenRefreshException;

    /**
     * Finds a refresh token in the DB by its string value
     *
     * @param token The string value of the refresh token
     * @return The refresh token, if it exists
     */
    Optional<RefreshToken> getByToken(String token);

    /**
     * Creates a new refresh token for a user
     *
     * @param userID The ID of the user
     * @return The new refresh token
     * @throws UserNotFoundException The user was not found
     */
    RefreshToken createRefreshToken(UUID userID) throws UserNotFoundException;

    /**
     * Verifies that the refresh token is not expired
     *
     * @param token The refresh token
     * @return The refresh token, if it is not expired
     * @throws TokenRefreshException The refresh token was expired
     */
    RefreshToken verifyExpiration(RefreshToken token) throws TokenRefreshException;

    /**
     * Deletes a refresh token from the DB by the user's ID
     *
     * @param userID The ID of the user
     * @return The number of refresh tokens deleted
     * @throws UserNotFoundException The user was not found
     */
    int deleteByUserID(UUID userID) throws UserNotFoundException;

    /**
     * Generates a response cookie
     *
     * @param name The name of the cookie
     * @param value The value of the cookie
     * @param path The path of the cookie
     * @return A response cookie
     */
    ResponseCookie generateCookie(String name, String value, String path);

    /**
     * Deletes an incoming refresh token in the request cookies from the DB
     *
     * @param req The incoming HTTP request
     */
    void deleteRefreshToken(HttpServletRequest req);
}
