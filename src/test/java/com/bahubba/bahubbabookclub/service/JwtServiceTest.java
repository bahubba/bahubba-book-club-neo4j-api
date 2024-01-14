package com.bahubba.bahubbabookclub.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.bahubba.bahubbabookclub.exception.TokenRefreshException;
import com.bahubba.bahubbabookclub.exception.UserNotFoundException;
import com.bahubba.bahubbabookclub.model.dto.AuthDTO;
import com.bahubba.bahubbabookclub.model.entity.RefreshToken;
import com.bahubba.bahubbabookclub.model.entity.User;
import com.bahubba.bahubbabookclub.repository.RefreshTokenRepo;
import com.bahubba.bahubbabookclub.repository.UserRepo;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseCookie;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;

/** Unit tests for the JwtService */
@SpringBootTest
@ActiveProfiles("test")
class JwtServiceTest {

    @Autowired
    JwtService jwtService;

    @MockBean
    private RefreshTokenRepo refreshTokenRepo;

    @MockBean
    private UserRepo userRepo;

    @Value("${app.properties.auth_cookie_name}")
    private String authCookieName;

    @Value("${app.properties.refresh_cookie_name}")
    private String refreshCookieName;

    @Value("${app.properties.secret_key}")
    private String secretKey;

    @Test
    void testGenerateJwtCookie() {
        ResponseCookie result =
                jwtService.generateJwtCookie(User.builder().username("name").build());

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(authCookieName);
        assertThat(result.toString().length()).isGreaterThan(0);
        assertThat(result.getPath()).isEqualTo("/api");
        assertThat(result.getMaxAge().getSeconds()).isEqualTo(24L * 60L * 60L);
        assertThat(result.isHttpOnly()).isTrue();
        assertThat(result.isSecure()).isTrue();
        assertThat(result.getDomain()).isNull();
        assertThat(result.getSameSite()).isEqualTo("None");
    }

    @Test
    void testGenerateJwtRefreshCookie() {
        ResponseCookie result = jwtService.generateJwtRefreshCookie("somecookie");

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(refreshCookieName);
        assertThat(result.toString().length()).isGreaterThan(0);
        assertThat(result.getPath()).isEqualTo("/api/v1/auth/refresh");
        assertThat(result.getMaxAge().getSeconds()).isEqualTo(24L * 60L * 60L);
        assertThat(result.isHttpOnly()).isTrue();
        assertThat(result.isSecure()).isTrue();
        assertThat(result.getDomain()).isNull();
        assertThat(result.getSameSite()).isEqualTo("None");
    }

    @Test
    void testGetJwtFromCookies() {
        MockHttpServletRequest mockReq = new MockHttpServletRequest();
        mockReq.setCookies(new Cookie(authCookieName, "foo"));

        String result = jwtService.getJwtFromCookies(mockReq);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo("foo");
    }

    @Test
    void testGetJwtRefreshFromCookies() {
        MockHttpServletRequest mockReq = new MockHttpServletRequest();
        mockReq.setCookies(new Cookie(refreshCookieName, "foo"));

        String result = jwtService.getJwtRefreshFromCookies(mockReq);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo("foo");
    }

    @Test
    void testExtractUsername() {
        String result = jwtService.extractUsername(Jwts.builder()
                .setSubject("someuser")
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey)))
                .compact());

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo("someuser");
    }

    @Test
    void testIsTokenValid() {
        boolean result = jwtService.isTokenValid(
                Jwts.builder()
                        .setSubject("someuser")
                        .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 hr validity
                        .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey)))
                        .compact(),
                User.builder().username("someuser").build());

        assertThat(result).isTrue();
    }

    @Test
    void testIsTokenValid_MismatchedName() {
        boolean result = jwtService.isTokenValid(
                Jwts.builder()
                        .setSubject("someuser")
                        .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                        .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey)))
                        .compact(),
                User.builder().username("someotheruser").build());

        assertThat(result).isFalse();
    }

    @Test
    void testRefreshTokenFromReq() {
        when(refreshTokenRepo.findByToken(anyString()))
                .thenReturn(Optional.of(RefreshToken.builder()
                        .user(User.builder().username("someuser").build())
                        .expiryDate(Instant.now().plusMillis(1000L * 60L * 60L))
                        .build()));

        MockHttpServletRequest mockReq = new MockHttpServletRequest();
        mockReq.setCookies(new Cookie(refreshCookieName, "sometoken"));

        AuthDTO result = jwtService.refreshToken(mockReq);

        verify(refreshTokenRepo, times(1)).findByToken(anyString());
        assertThat(result).isNotNull();
        assertThat(result.getUser()).isNotNull();
        assertThat(result.getToken()).isNotNull();
        assertThat(result.getRefreshToken()).isNotNull();
    }

    // TODO - Add test for exception from missing User in token
    @Test
    void testRefreshToken() {
        when(refreshTokenRepo.findByToken(anyString()))
                .thenReturn(Optional.of(RefreshToken.builder()
                        .user(User.builder().username("someuser").build())
                        .expiryDate(Instant.now().plusMillis(1000L * 60L * 60L))
                        .build()));

        AuthDTO result = jwtService.refreshToken("sometoken");

        verify(refreshTokenRepo, times(1)).findByToken(anyString());
        assertThat(result).isNotNull();
        assertThat(result.getUser()).isNotNull();
        assertThat(result.getToken()).isNotNull();
        assertThat(result.getRefreshToken()).isNotNull();
    }

    @Test
    void testRefreshToken_expired() {
        when(refreshTokenRepo.findByToken(anyString()))
                .thenReturn(Optional.of(RefreshToken.builder()
                        .user(User.builder().username("someuser").build())
                        .expiryDate(Instant.now().minusMillis(1000L * 60L * 60L))
                        .build()));

        // Test that the exception is thrown
        assertThatThrownBy(() -> jwtService.refreshToken("sometoken"))
                .isInstanceOf(TokenRefreshException.class)
                .hasMessageContaining("Refresh token expired");
    }

    @Test
    void testRefreshToken_userNotFound() {
        when(refreshTokenRepo.findByToken(anyString())).thenReturn(Optional.empty());

        // Test that the exception is thrown
        assertThatThrownBy(() -> jwtService.refreshToken("sometoken"))
                .isInstanceOf(TokenRefreshException.class)
                .hasMessageContaining("Refresh token missing");
    }

    @Test
    void testRefreshToken_missingToken() {
        assertThatThrownBy(() -> jwtService.refreshToken(new MockHttpServletRequest()))
                .isInstanceOf(TokenRefreshException.class)
                .hasMessageContaining("Refresh token missing");
    }

    @Test
    void testCreateRefreshToken() {
        when(userRepo.findById(any(UUID.class)))
                .thenReturn(Optional.of(User.builder().username("someuser").build()));

        when(refreshTokenRepo.save(any(RefreshToken.class)))
                .thenReturn(RefreshToken.builder()
                        .token("sometoken")
                        .expiryDate(Instant.now().plusMillis(1000L * 60L * 60L))
                        .build());

        RefreshToken result = jwtService.createRefreshToken(UUID.randomUUID());

        verify(refreshTokenRepo, times(1)).save(any(RefreshToken.class));
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("sometoken");
        assertThat(result.getExpiryDate()).isNotNull();
    }

    @Test
    void testCreateRefreshToken_userNotFound() {
        when(userRepo.findById(any(UUID.class))).thenReturn(Optional.empty());

        // Test that the exception is thrown
        assertThatThrownBy(() -> jwtService.createRefreshToken(UUID.randomUUID()))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User could not be found");
    }

    @Test
    void testDeleteByUserID() {
        when(userRepo.findById(any(UUID.class)))
                .thenReturn(Optional.of(User.builder().username("someuser").build()));

        jwtService.deleteByUserID(UUID.randomUUID());

        verify(refreshTokenRepo, times(1)).deleteByUser(any(User.class));
    }

    @Test
    void testDeleteByUserId_userNotFound() {
        when(userRepo.findById(any(UUID.class))).thenReturn(Optional.empty());

        // Test that the exception is thrown
        assertThatThrownBy(() -> jwtService.deleteByUserID(UUID.randomUUID()))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User could not be found");
    }

    @Test
    void testDeleteRefreshToken() {
        when(refreshTokenRepo.findByToken(anyString()))
                .thenReturn(Optional.of(RefreshToken.builder()
                        .user(User.builder().username("someuser").build())
                        .expiryDate(Instant.now().plusMillis(1000L * 60L * 60L))
                        .build()));

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setCookies(new Cookie(refreshCookieName, "sometoken"));

        jwtService.deleteRefreshToken(req);

        verify(refreshTokenRepo, times(1)).findByToken(anyString());
        verify(refreshTokenRepo, times(1)).delete(any(RefreshToken.class));
    }
}
