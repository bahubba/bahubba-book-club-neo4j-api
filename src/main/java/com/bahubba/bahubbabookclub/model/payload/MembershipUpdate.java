package com.bahubba.bahubbabookclub.model.payload;

import com.bahubba.bahubbabookclub.model.enums.BookClubRole;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Data sent with HTTP request for updating a user's membership in a book club */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MembershipUpdate {
    private String bookClubName;
    private UUID userID;
    private BookClubRole role;
}
