package com.bahubba.bahubbabookclub.service.impl;

import com.bahubba.bahubbabookclub.service.S3Service;
import com.bahubba.bahubbabookclub.util.APIConstants;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Service
@Transactional
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3PreSigner;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Override
    public List<S3Object> listS3ObjectsAtPrefix(String prefix) {
        return s3Client.listObjectsV2(builder -> builder.bucket(bucket).prefix(prefix))
                .contents();
    }

    @Override
    public String getPreSignedURL(String key) {
        GetObjectPresignRequest preSignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.between(
                        Instant.now(),
                        Instant.now().plus(Duration.ofMinutes(APIConstants.BOOK_CLUB_IMAGE_URL_TIMEOUT_MINUTES))))
                .getObjectRequest(
                        getObjectRequest -> getObjectRequest.bucket(bucket).key(key))
                .build();

        return s3PreSigner.presignGetObject(preSignRequest).url().toString();
    }
}
