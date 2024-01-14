package com.bahubba.bahubbabookclub.service.impl;

import com.bahubba.bahubbabookclub.exception.UserNotFoundException;
import com.bahubba.bahubbabookclub.model.dto.AuthDTO;
import com.bahubba.bahubbabookclub.model.entity.Notification;
import com.bahubba.bahubbabookclub.model.entity.User;
import com.bahubba.bahubbabookclub.model.enums.NotificationType;
import com.bahubba.bahubbabookclub.model.mapper.UserMapper;
import com.bahubba.bahubbabookclub.model.payload.AuthRequest;
import com.bahubba.bahubbabookclub.model.payload.UserPayload;
import com.bahubba.bahubbabookclub.repository.NotificationRepo;
import com.bahubba.bahubbabookclub.repository.UserRepo;
import com.bahubba.bahubbabookclub.service.AuthService;
import com.bahubba.bahubbabookclub.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

/** Registration and authentication logic */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    @Value("${app.properties.auth_cookie_name}")
    private String authCookieName;

    @Value("${app.properties.refresh_cookie_name}")
    private String refreshCookieName;

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UserRepo userRepo;
    private final NotificationRepo notificationRepo;
    private final UserMapper userMapper;

    @Override
    public AuthDTO register(UserPayload newUser) throws UserNotFoundException {
        // Generate and persist a User entity
        User user = userRepo.save(userMapper.payloadToEntity(newUser));

        // Generate and persist a notification
        notificationRepo.save(Notification.builder()
                .sourceUser(user)
                .targetUser(user)
                .type(NotificationType.NEW_USER)
                .build());

        // Generate auth and refresh JWTs
        ResponseCookie jwtCookie = jwtService.generateJwtCookie(user);
        ResponseCookie refreshCookie = jwtService.generateJwtRefreshCookie(
                jwtService.createRefreshToken(user.getId()).getToken());

        // Return the user's stored info and JWTs
        return AuthDTO.builder()
                .user(userMapper.entityToDTO(user))
                .token(jwtCookie)
                .refreshToken(refreshCookie)
                .build();
    }

    @Override
    public AuthDTO authenticate(@NotNull AuthRequest req) throws AuthenticationException, UserNotFoundException {
        authManager.authenticate(new UsernamePasswordAuthenticationToken(req.getUsernameOrEmail(), req.getPassword()));

        User user = userRepo.findByUsernameOrEmail(req.getUsernameOrEmail(), req.getUsernameOrEmail())
                .orElseThrow(() -> new UserNotFoundException(req.getUsernameOrEmail()));

        ResponseCookie jwtCookie = jwtService.generateJwtCookie(user);

        // Delete existing refresh cookies
        jwtService.deleteByUserID(user.getId());

        ResponseCookie refreshCookie = jwtService.generateJwtRefreshCookie(
                jwtService.createRefreshToken(user.getId()).getToken());

        return AuthDTO.builder()
                .user(userMapper.entityToDTO(user))
                .token(jwtCookie)
                .refreshToken(refreshCookie)
                .build();
    }

    @Override
    public AuthDTO logout(HttpServletRequest req) {
        jwtService.deleteRefreshToken(req);

        return AuthDTO.builder()
                .token(jwtService.generateCookie(authCookieName, "", ""))
                .refreshToken(jwtService.generateCookie(refreshCookieName, "", ""))
                .build();
    }
}
