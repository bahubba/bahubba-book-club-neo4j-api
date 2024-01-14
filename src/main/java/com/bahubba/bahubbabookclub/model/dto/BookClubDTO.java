package com.bahubba.bahubbabookclub.model.dto;

import com.bahubba.bahubbabookclub.model.enums.Publicity;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Book club information to be returned to clients */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookClubDTO {
    private UUID id;
    private String name;
    private S3ImageDTO image;
    private String description;
    private Publicity publicity;
    private LocalDateTime created;
    private LocalDateTime disbanded;
}
