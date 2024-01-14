package com.bahubba.bahubbabookclub.model.dto;

import com.bahubba.bahubbabookclub.model.enums.BookClubRole;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Book club membership info to be returned to clients */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookClubMembershipDTO {
    private BookClubDTO bookClub;
    private UserDTO user;
    private BookClubRole clubRole;

    @JsonProperty("isOwner")
    private boolean isOwner;

    private LocalDateTime joined;
    private LocalDateTime departed;
}
