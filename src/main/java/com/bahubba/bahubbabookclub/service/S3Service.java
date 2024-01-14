package com.bahubba.bahubbabookclub.service;

import java.util.List;
import software.amazon.awssdk.services.s3.model.S3Object;

/**
 * S3 service layer
 */
public interface S3Service {
    /**
     * Get a list of objects from S3 with a given prefix
     *
     * @param prefix The prefix to search for
     * @return The list of objects
     */
    List<S3Object> listS3ObjectsAtPrefix(String prefix);

    /**
     * Get a pre-signed URL for a file from S3
     *
     * @param key The key of the file to get
     * @return The pre-signed URL
     */
    String getPreSignedURL(String key);
}
