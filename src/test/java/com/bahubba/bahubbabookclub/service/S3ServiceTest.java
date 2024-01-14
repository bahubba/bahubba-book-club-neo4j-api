package com.bahubba.bahubbabookclub.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.http.SdkHttpMethod;
import software.amazon.awssdk.http.SdkHttpRequest;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@SpringBootTest
@ActiveProfiles("test")
class S3ServiceTest {
    @Autowired
    S3Service s3Service;

    @MockBean
    S3Client s3Client;

    @MockBean
    S3Presigner s3PreSigner;

    @Test
    void testListS3ObjectsAtPrefix() {
        when(s3Client.listObjectsV2(any(Consumer.class)))
                .thenReturn(ListObjectsV2Response.builder()
                        .contents(List.of(S3Object.builder().build()))
                        .build());
        List<S3Object> result = s3Service.listS3ObjectsAtPrefix("test");
        assertThat(result).isNotNull();
    }

    @Test
    void testGetPreSignedURL() {
        when(s3PreSigner.presignGetObject(any(GetObjectPresignRequest.class)))
                .thenReturn(PresignedGetObjectRequest.builder()
                        .httpRequest(SdkHttpRequest.builder()
                                .method(SdkHttpMethod.GET)
                                .host("localhost")
                                .protocol("https")
                                .build())
                        .signedHeaders(Map.of("someHeader", List.of("someVal")))
                        .isBrowserExecutable(false)
                        .expiration(Instant.now().plus(Duration.ofSeconds(1)))
                        .build());
        String result = s3Service.getPreSignedURL("test");
        assertThat(result).isNotNull();
    }
}
