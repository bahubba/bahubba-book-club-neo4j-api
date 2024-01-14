package com.bahubba.bahubbabookclub.model.payload;

import com.bahubba.bahubbabookclub.model.enums.Publicity;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Data sent with HTTP request for creating a new book club */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookClubPayload {
    private UUID id;
    private String name;
    private String imageFileName;
    private String description;
    private Publicity publicity;
}
