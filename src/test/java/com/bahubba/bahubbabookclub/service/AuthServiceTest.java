package com.bahubba.bahubbabookclub.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.bahubba.bahubbabookclub.exception.UserNotFoundException;
import com.bahubba.bahubbabookclub.model.dto.AuthDTO;
import com.bahubba.bahubbabookclub.model.entity.Notification;
import com.bahubba.bahubbabookclub.model.entity.RefreshToken;
import com.bahubba.bahubbabookclub.model.entity.User;
import com.bahubba.bahubbabookclub.model.enums.Role;
import com.bahubba.bahubbabookclub.model.payload.AuthRequest;
import com.bahubba.bahubbabookclub.model.payload.UserPayload;
import com.bahubba.bahubbabookclub.repository.NotificationRepo;
import com.bahubba.bahubbabookclub.repository.RefreshTokenRepo;
import com.bahubba.bahubbabookclub.repository.UserRepo;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseCookie;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class AuthServiceTest {
    @Autowired
    AuthService authService;

    @MockBean
    UserRepo userRepo;

    @MockBean
    JwtService jwtService;

    @MockBean
    AuthenticationManager authManager;

    @MockBean
    RefreshTokenRepo refreshTokenRepo;

    @MockBean
    NotificationRepo notificationRepo;

    @Test
    void testRegister() {
        UUID tstUUID = UUID.randomUUID();
        when(userRepo.save(any(User.class)))
                .thenReturn(User.builder()
                        .id(tstUUID)
                        .username("user")
                        .email("foo@bar.foo")
                        .givenName("Foo")
                        .surname("Bar")
                        .joined(LocalDateTime.now())
                        .role(Role.USER)
                        .password("password")
                        .build());

        when(jwtService.createRefreshToken(any(UUID.class)))
                .thenReturn(RefreshToken.builder().token("foobar").build());

        when(jwtService.generateJwtCookie(any(User.class)))
                .thenReturn(ResponseCookie.from("foo", "bar").build());

        AuthDTO result =
                authService.register(UserPayload.builder().password("password").build());

        verify(notificationRepo, times(1)).save(any(Notification.class));
        verify(jwtService, times(1)).generateJwtCookie(any(User.class));
    }

    @Test
    void testAuthenticate() {
        UUID tstUUID = UUID.randomUUID();
        when(userRepo.findByUsernameOrEmail(anyString(), anyString()))
                .thenReturn(Optional.of(User.builder()
                        .id(tstUUID)
                        .username("user")
                        .email("foo@bar.foo")
                        .givenName("Foo")
                        .surname("Bar")
                        .joined(LocalDateTime.now())
                        .role(Role.USER)
                        .password("password")
                        .build()));

        when(userRepo.findById(any(UUID.class)))
                .thenReturn(Optional.of(User.builder()
                        .id(tstUUID)
                        .username("user")
                        .email("foo@bar.foo")
                        .givenName("Foo")
                        .surname("Bar")
                        .joined(LocalDateTime.now())
                        .role(Role.USER)
                        .password("password")
                        .build()));

        when(jwtService.createRefreshToken(any(UUID.class)))
                .thenReturn(RefreshToken.builder().token("foobar").build());

        when(jwtService.generateJwtCookie(any(User.class)))
                .thenReturn(ResponseCookie.from("foo", "bar").build());

        AuthDTO result = authService.authenticate(AuthRequest.builder()
                .usernameOrEmail("username")
                .password("password")
                .build());

        verify(jwtService, times(1)).generateJwtCookie(any(User.class));
    }

    @Test
    void testAuthenticate_UserNotFound() {
        when(userRepo.findByUsernameOrEmail(anyString(), anyString())).thenReturn(Optional.empty());
        assertThrows(
                UserNotFoundException.class,
                () -> authService.authenticate(AuthRequest.builder()
                        .usernameOrEmail("username")
                        .password("password")
                        .build()));
    }

    @Test
    void testLogout() {
        when(jwtService.generateCookie(anyString(), anyString(), anyString()))
                .thenReturn(ResponseCookie.from("foo", "").build());

        AuthDTO result = authService.logout(new MockHttpServletRequest());

        verify(jwtService, times(1)).deleteRefreshToken(any(HttpServletRequest.class));
        verify(jwtService, times(2)).generateCookie(anyString(), anyString(), anyString());
        assertThat(result.getToken()).isNotNull();
        assertThat(result.getToken().getValue()).isEmpty();
        assertThat(result.getRefreshToken()).isNotNull();
        assertThat(result.getRefreshToken().getValue()).isEmpty();
    }
}
