package com.bahubba.bahubbabookclub.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseCookie;

/** Authentication information to be returned to clients */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthDTO {
    UserDTO user;
    ResponseCookie token;
    ResponseCookie refreshToken;
}
