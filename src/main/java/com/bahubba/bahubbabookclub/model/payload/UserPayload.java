package com.bahubba.bahubbabookclub.model.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Data sent with HTTP request for creating a new user */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserPayload {
    private String username;
    private String email;
    private String givenName;
    private String middleName;
    private String surname;
    private String suffix;
    private String title;
    private String password;
}
