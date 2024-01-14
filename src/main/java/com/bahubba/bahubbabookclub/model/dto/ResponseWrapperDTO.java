package com.bahubba.bahubbabookclub.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Wrapper for responses to include a message and data
 *
 * @param <T> data type
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseWrapperDTO<T> {
    private String message;
    private T data;
}
