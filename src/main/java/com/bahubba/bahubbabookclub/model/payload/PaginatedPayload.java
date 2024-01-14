package com.bahubba.bahubbabookclub.model.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/** Pagination info sent with HTTP requests expecting paginated responses */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class PaginatedPayload {
    private int pageNum;
    private int pageSize;
}
