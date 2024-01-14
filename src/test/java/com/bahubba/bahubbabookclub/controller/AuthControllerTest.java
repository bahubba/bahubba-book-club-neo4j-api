package com.bahubba.bahubbabookclub.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.bahubba.bahubbabookclub.exception.TokenRefreshException;
import com.bahubba.bahubbabookclub.model.dto.AuthDTO;
import com.bahubba.bahubbabookclub.model.dto.MessageResponseDTO;
import com.bahubba.bahubbabookclub.model.dto.ResponseWrapperDTO;
import com.bahubba.bahubbabookclub.model.dto.UserDTO;
import com.bahubba.bahubbabookclub.model.payload.AuthRequest;
import com.bahubba.bahubbabookclub.model.payload.UserPayload;
import com.bahubba.bahubbabookclub.service.AuthService;
import com.bahubba.bahubbabookclub.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;

/** Unit tests for the authentication endpoints */
@SpringBootTest
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    AuthController authController;

    @MockBean
    AuthService authService;

    @MockBean
    JwtService jwtService;

    @Test
    void testRegisterUser() {
        when(authService.register(any(UserPayload.class)))
                .thenReturn(AuthDTO.builder()
                        .user(new UserDTO())
                        .token(ResponseCookie.from("foo", "bar").build())
                        .refreshToken(ResponseCookie.from("bar", "foo").build())
                        .build());

        ResponseEntity<ResponseWrapperDTO<UserDTO>> rsp = authController.register(new UserPayload());

        verify(authService, times(1)).register(any(UserPayload.class));
        assertThat(rsp).isNotNull();
        assertThat(rsp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(rsp.getBody()).isNotNull();
        assertThat(rsp.getBody().getData()).isNotNull();
        assertThat(rsp.getBody().getData()).isNotNull();
    }

    @Test
    void testRegister_duplicateUsernameOrEmail() {
        when(authService.register(any(UserPayload.class))).thenThrow(new DataIntegrityViolationException("some error"));

        ResponseEntity<ResponseWrapperDTO<UserDTO>> rsp = authController.register(new UserPayload());

        verify(authService, times(1)).register(any(UserPayload.class));
        assertThat(rsp).isNotNull();
        assertThat(rsp.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(rsp.getBody()).isNotNull();
        assertThat(rsp.getBody().getMessage()).isNotNull();
        assertThat(rsp.getBody().getData()).isNull();
    }

    @Test
    void testAuthenticate() {
        when(authService.authenticate(any(AuthRequest.class)))
                .thenReturn(AuthDTO.builder()
                        .user(new UserDTO())
                        .token(ResponseCookie.from("foo", "bar").build())
                        .refreshToken(ResponseCookie.from("bar", "foo").build())
                        .build());

        ResponseEntity<ResponseWrapperDTO<UserDTO>> rsp = authController.authenticate(new AuthRequest());

        verify(authService, times(1)).authenticate(any(AuthRequest.class));
        assertThat(rsp).isNotNull();
        assertThat(rsp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(rsp.getBody()).isNotNull();
        assertThat(rsp.getBody().getMessage()).isNotNull();
        assertThat(rsp.getBody().getData()).isNotNull();
    }

    @Test
    void testAuthenticate_invalidCredentials() {
        when(authService.authenticate(any(AuthRequest.class))).thenThrow(new BadCredentialsException("some error"));

        ResponseEntity<ResponseWrapperDTO<UserDTO>> rsp = authController.authenticate(new AuthRequest());

        verify(authService, times(1)).authenticate(any(AuthRequest.class));
        assertThat(rsp).isNotNull();
        assertThat(rsp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(rsp.getBody()).isNotNull();
        assertThat(rsp.getBody().getMessage()).isNotNull();
        assertThat(rsp.getBody().getData()).isNull();
    }

    @Test
    void testRefreshToken() {
        when(jwtService.refreshToken(any(HttpServletRequest.class)))
                .thenReturn(AuthDTO.builder()
                        .token(ResponseCookie.from("foo", "bar").build())
                        .refreshToken(ResponseCookie.from("bar", "foo").build())
                        .build());

        ResponseEntity<MessageResponseDTO> rsp = authController.refreshToken(new MockHttpServletRequest());

        verify(jwtService, times(1)).refreshToken(any(HttpServletRequest.class));
        assertThat(rsp.getBody()).isNotNull();
    }

    @Test
    void testRefreshToken_invalidRefreshToken() {
        when(jwtService.refreshToken(any(HttpServletRequest.class)))
                .thenThrow(new TokenRefreshException("sometoken", "some error"));

        ResponseEntity<MessageResponseDTO> rsp = authController.refreshToken(new MockHttpServletRequest());

        verify(jwtService, times(1)).refreshToken(any(HttpServletRequest.class));
        assertThat(rsp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(rsp.getBody()).isNotNull();
        assertThat(rsp.getBody().getMessage()).contains("some error");
    }

    @Test
    void testLogout() {
        when(authService.logout(any(HttpServletRequest.class)))
                .thenReturn(AuthDTO.builder()
                        .token(ResponseCookie.from("foo", "bar").build())
                        .refreshToken(ResponseCookie.from("bar", "foo").build())
                        .build());

        ResponseEntity<MessageResponseDTO> rsp = authController.logout(new MockHttpServletRequest());

        verify(authService, times(1)).logout(any(HttpServletRequest.class));
        assertThat(rsp).isNotNull();
        assertThat(rsp.getBody()).isNotNull();
        assertThat(rsp.getBody().getMessage()).isNotNull();
    }
}
