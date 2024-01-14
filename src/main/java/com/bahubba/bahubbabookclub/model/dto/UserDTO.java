package com.bahubba.bahubbabookclub.model.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** User information to be returned to clients */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private UUID id;
    private String username;
    private String email;
    private String givenName;
    private String middleName;
    private String surname;
    private String suffix;
    private String title;
    private LocalDateTime joined;
    private LocalDateTime departed;
}
