package com.bahubba.bahubbabookclub.exception;

import lombok.Getter;
import org.springframework.data.domain.Page;

/** Custom exception for when page size is < 1 */
@Getter
public class PageSizeTooSmallException extends RuntimeException {
    private final transient Page<?> payload;

    /**
     * Generates an exception for a page size that is < 1
     *
     * @param defaultPageSize the page size being defaulted to
     * @param payload the payload being returned
     */
    public PageSizeTooSmallException(int defaultPageSize, Page<?> payload) {
        super("Page size must be positive (greater than 0); Defaulting to a page size of " + defaultPageSize);
        this.payload = payload;
    }
}
