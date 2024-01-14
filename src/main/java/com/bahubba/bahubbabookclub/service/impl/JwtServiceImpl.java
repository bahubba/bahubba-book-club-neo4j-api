package com.bahubba.bahubbabookclub.service.impl;

import com.bahubba.bahubbabookclub.exception.TokenRefreshException;
import com.bahubba.bahubbabookclub.exception.UserNotFoundException;
import com.bahubba.bahubbabookclub.model.dto.AuthDTO;
import com.bahubba.bahubbabookclub.model.entity.RefreshToken;
import com.bahubba.bahubbabookclub.model.entity.User;
import com.bahubba.bahubbabookclub.model.mapper.UserMapper;
import com.bahubba.bahubbabookclub.repository.RefreshTokenRepo;
import com.bahubba.bahubbabookclub.repository.UserRepo;
import com.bahubba.bahubbabookclub.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import java.security.Key;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.WebUtils;

/** JWT service layer */
@Service
@Transactional
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {
    @Value("${app.properties.secret_key}")
    private String secretKey;

    @Value("${app.properties.auth_cookie_name}")
    private String authCookieName;

    @Value("${app.properties.refresh_cookie_name}")
    private String refreshCookieName;

    private final RefreshTokenRepo refreshTokenRepo;
    private final UserRepo userRepo;
    private final UserMapper userMapper;

    @Override
    public ResponseCookie generateJwtCookie(UserDetails userDetails) {
        String jwt = generateToken(new HashMap<>(), userDetails);
        return generateCookie(authCookieName, jwt, "/api");
    }

    @Override
    public ResponseCookie generateJwtRefreshCookie(String refreshToken) {
        return generateCookie(refreshCookieName, refreshToken, "/api/v1/auth/refresh");
    }

    @Override
    public String getJwtFromCookies(HttpServletRequest req) {
        return getCookieValueByName(req, authCookieName);
    }

    @Override
    public String getJwtRefreshFromCookies(HttpServletRequest req) {
        return getCookieValueByName(req, refreshCookieName);
    }

    @Override
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // TODO - Update validity checks with error handling (see
    // https://www.bezkoder.com/spring-security-refresh-token/)
    @Override
    public boolean isTokenValid(String token, @NotNull UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    @Override
    public <T> T extractClaim(String token, @NotNull Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    @Override
    public AuthDTO refreshToken(HttpServletRequest req) throws TokenRefreshException {
        String refreshToken = getJwtRefreshFromCookies(req);
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new TokenRefreshException(refreshToken, "Refresh token missing");
        }
        return refreshToken(getJwtRefreshFromCookies(req));
    }

    @Override
    public AuthDTO refreshToken(String refreshToken) throws TokenRefreshException {
        return getByToken(refreshToken)
                .map(this::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    ResponseCookie jwtCookie = this.generateJwtCookie(user);

                    return AuthDTO.builder()
                            .user(userMapper.entityToDTO(user))
                            .token(jwtCookie)
                            .refreshToken(this.generateJwtRefreshCookie(refreshToken))
                            .build();
                })
                .orElseThrow(() -> new TokenRefreshException(refreshToken, "Refresh token missing"));
    }

    @Override
    public Optional<RefreshToken> getByToken(String token) {
        return refreshTokenRepo.findByToken(token);
    }

    @Override
    public RefreshToken createRefreshToken(UUID userID) throws UserNotFoundException {
        // Get the current user
        User user = userRepo.findById(userID).orElseThrow(() -> new UserNotFoundException(userID));

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(1000L * 60L * 60L))
                .build();

        return refreshTokenRepo.save(refreshToken);
    }

    @Override
    public RefreshToken verifyExpiration(@NotNull RefreshToken token) throws TokenRefreshException {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepo.delete(token);
            throw new TokenRefreshException(token.getToken(), "Refresh token expired");
        }

        return token;
    }

    // TODO - Handle the exception, it shouldn't bubble up, as it doesn't matter if they don't have
    // existing refresh tokens
    @Override
    public int deleteByUserID(UUID userID) throws UserNotFoundException {
        return refreshTokenRepo.deleteByUser(
                userRepo.findById(userID).orElseThrow(() -> new UserNotFoundException(userID)));
    }

    @Override
    public ResponseCookie generateCookie(String name, String value, String path) {
        return ResponseCookie.from(name, value)
                .path(path)
                .maxAge(24L * 60L * 60L)
                .httpOnly(true)
                .secure(true)
                .domain(null)
                .sameSite("None")
                .build();
    }

    @Override
    public void deleteRefreshToken(HttpServletRequest req) {
        String refreshToken = getJwtRefreshFromCookies(req);
        if (refreshToken != null && !refreshToken.isEmpty()) {
            Optional<RefreshToken> refreshTokenEntity = refreshTokenRepo.findByToken(refreshToken);
            refreshTokenEntity.ifPresent(refreshTokenRepo::delete);
        }
    }

    /**
     * Generates a JWT token
     *
     * @param extraClaims Extra claims to add to the token
     * @param userDetails The user's details
     * @return A string JWT token
     */
    private String generateToken(Map<String, Object> extraClaims, @NotNull UserDetails userDetails) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 hr validity
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Gets a cookie from an incoming HTTP request by name
     *
     * @param req The incoming HTTP request
     * @param name The name of the cookie
     * @return The cookie's value
     */
    @Nullable private String getCookieValueByName(HttpServletRequest req, String name) {
        Cookie cookie = WebUtils.getCookie(req, name);
        if (cookie != null) {
            return cookie.getValue();
        }

        return null;
    }

    // FIXME - Gracefully handle io.jsonwebtoken.ExpiredJwtException

    /**
     * Extracts all claims from a JWT token
     *
     * @param token The JWT token
     * @return All claims from the JWT token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Generates a signing key for JWTs using a secret key
     *
     * @return A JWT signing key
     */
    private @NotNull Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Checks if a JWT token is expired
     *
     * @param token The JWT token
     * @return Whether the JWT token is expired
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extracts the expiration date from a JWT token
     *
     * @param token The JWT token
     * @return The expiration date of the JWT token
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}
