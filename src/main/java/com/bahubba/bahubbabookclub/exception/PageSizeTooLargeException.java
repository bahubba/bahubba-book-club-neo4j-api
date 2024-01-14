package com.bahubba.bahubbabookclub.exception;

import lombok.Getter;
import org.springframework.data.domain.Page;

/** Custom exception for when page size is some (arbitrary?) amount that is too large */
@Getter
public class PageSizeTooLargeException extends RuntimeException {
    private final transient Page<?> payload;

    /**
     * Generates exception for page size being an arbitrary, too-large number
     *
     * @param maxPageSize the maximum page size
     * @param defaultPageSize the page size being defaulted to
     * @param payload the page to return using the provided page number and defaulted page size
     */
    public PageSizeTooLargeException(int maxPageSize, int defaultPageSize, Page<?> payload) {
        super("Page size must be less than " + maxPageSize + "; Defaulting to a page size of " + defaultPageSize);
        this.payload = payload;
    }
}
