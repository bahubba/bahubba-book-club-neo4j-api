package com.bahubba.bahubbabookclub.model.payload;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Data sent with HTTP request for changing ownership of a book club */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewOwner {
    private String bookClubName;
    private UUID newOwnerID;
}
