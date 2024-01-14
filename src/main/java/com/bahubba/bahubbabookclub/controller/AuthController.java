package com.bahubba.bahubbabookclub.controller;

import com.bahubba.bahubbabookclub.exception.TokenRefreshException;
import com.bahubba.bahubbabookclub.exception.UserNotFoundException;
import com.bahubba.bahubbabookclub.model.dto.AuthDTO;
import com.bahubba.bahubbabookclub.model.dto.MessageResponseDTO;
import com.bahubba.bahubbabookclub.model.dto.ResponseWrapperDTO;
import com.bahubba.bahubbabookclub.model.dto.UserDTO;
import com.bahubba.bahubbabookclub.model.payload.AuthRequest;
import com.bahubba.bahubbabookclub.model.payload.UserPayload;
import com.bahubba.bahubbabookclub.service.AuthService;
import com.bahubba.bahubbabookclub.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Authentication endpoints */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth Controller", description = "Auth endpoints")
@RequiredArgsConstructor
@Log4j2
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    /**
     * Registers a user
     *
     * @param newUser New user information
     * @return Persisted user information
     * @throws UserNotFoundException The user was not found
     */
    @PostMapping("/register")
    @Operation(summary = "Register", description = "Registers a user")
    public ResponseEntity<ResponseWrapperDTO<UserDTO>> register(@RequestBody UserPayload newUser)
            throws UserNotFoundException {

        try {
            AuthDTO authDTO = authService.register(newUser);

            // On success, return the user's info and JWTs in HTTP-Only cookies
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, authDTO.getToken().toString())
                    .header(HttpHeaders.SET_COOKIE, authDTO.getRefreshToken().toString())
                    .body(ResponseWrapperDTO.<UserDTO>builder()
                            .message("User registered successfully")
                            .data(authDTO.getUser())
                            .build());
        } catch (DataIntegrityViolationException e) {
            // On failure, return a message indicating the username or email already exists
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ResponseWrapperDTO.<UserDTO>builder()
                            .message("Username or email already exists")
                            .build());
        }
    }

    /**
     * Accepts user credentials and returns auth and refresh JWTs in HTTP-Only cookies
     *
     * @param req User credentials (username and password)
     * @return The user's stored info and JWTs
     */
    @PostMapping("/authenticate")
    @Operation(
            summary = "Authenticate/Log In",
            description = "Accepts user credentials and returns auth and refresh JWTs in HTTP-Only cookies")
    public ResponseEntity<ResponseWrapperDTO<UserDTO>> authenticate(@RequestBody AuthRequest req) {
        try {
            AuthDTO authDTO = authService.authenticate(req);

            // On success, return the user's info and JWTs in HTTP-Only cookies
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, authDTO.getToken().toString())
                    .header(HttpHeaders.SET_COOKIE, authDTO.getRefreshToken().toString())
                    .body(ResponseWrapperDTO.<UserDTO>builder()
                            .message("User authenticated successfully")
                            .data(authDTO.getUser())
                            .build());
        } catch (BadCredentialsException e) {
            log.error("Login error: " + e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ResponseWrapperDTO.<UserDTO>builder()
                            .message("Invalid credentials")
                            .build());
        } catch (Exception e) {
            log.error("Some other login error: " + e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ResponseWrapperDTO.<UserDTO>builder()
                            .message("Invalid credentials")
                            .build());
        }
    }

    /**
     * Generates a new auth (and refresh) token based on a valid refresh token
     *
     * @param req HTTP request from the client
     * @return A message with success status of the re-authentication
     */
    @PostMapping("/refresh")
    @Operation(
            summary = "Refresh Authentication",
            description = "Generates a new auth (and refresh) token based on a valid refresh token")
    public ResponseEntity<MessageResponseDTO> refreshToken(HttpServletRequest req) {

        try {
            AuthDTO authDTO = jwtService.refreshToken(req);

            // On success, return the user's info and refreshed JWTs in HTTP-Only cookies
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, authDTO.getToken().toString())
                    .header(HttpHeaders.SET_COOKIE, authDTO.getRefreshToken().toString())
                    .body(MessageResponseDTO.builder()
                            .message("Token refreshed successfully")
                            .build());
        } catch (TokenRefreshException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(MessageResponseDTO.builder().message(e.getMessage()).build());
        }
    }

    /**
     * Logs out the user by deleting the auth and refresh tokens
     *
     * @param req HTTP request from the client
     * @return A message with success status of the logout
     */
    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Logs out the user by deleting the auth and refresh tokens")
    public ResponseEntity<MessageResponseDTO> logout(HttpServletRequest req) {
        AuthDTO authDTO = authService.logout(req);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, authDTO.getToken().toString())
                .header(HttpHeaders.SET_COOKIE, authDTO.getRefreshToken().toString())
                .body(MessageResponseDTO.builder().message("Logout successful").build());
    }
}
