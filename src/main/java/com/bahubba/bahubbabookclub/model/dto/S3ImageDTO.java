package com.bahubba.bahubbabookclub.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class S3ImageDTO {
    private String fileName;
    private String url;
}
