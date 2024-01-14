package com.bahubba.bahubbabookclub.model.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/** Data sent with HTTP request for searching for book clubs */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class BookClubSearch extends PaginatedPayload {
    private String searchTerm;
}
