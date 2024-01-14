package com.bahubba.bahubbabookclub.aspect;

import com.bahubba.bahubbabookclub.model.dto.BookClubDTO;
import com.bahubba.bahubbabookclub.model.dto.S3ImageDTO;
import com.bahubba.bahubbabookclub.service.S3Service;
import com.bahubba.bahubbabookclub.util.APIConstants;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class BookClubAspect {
    private final S3Service s3Service;

    @AfterReturning(
            pointcut =
                    "execution(com.bahubba.bahubbabookclub.model.dto.BookClubDTO com.bahubba.bahubbabookclub.service.*.*(..))",
            returning = "bookClubDTO")
    public void addPreSignedURL(JoinPoint joinPoint, @NotNull BookClubDTO bookClubDTO) {
        bookClubDTO.setImage(S3ImageDTO.builder()
                .fileName(bookClubDTO.getImage().getFileName())
                .url(s3Service.getPreSignedURL(APIConstants.BOOK_CLUB_STOCK_IMAGE_PREFIX
                        + bookClubDTO.getImage().getFileName()))
                .build());
    }

    @AfterReturning(
            pointcut =
                    "execution(org.springframework.data.domain.Page<com.bahubba.bahubbabookclub.model.dto.BookClubDTO> com.bahubba.bahubbabookclub.service.*.*(..))",
            returning = "bookClubDTOs")
    public void addPreSignedURL(JoinPoint joinPoint, @NotNull Page<BookClubDTO> bookClubDTOs) {
        bookClubDTOs.forEach(bookClubDTO -> bookClubDTO.setImage(S3ImageDTO.builder()
                .fileName(bookClubDTO.getImage().getFileName())
                .url(s3Service.getPreSignedURL(APIConstants.BOOK_CLUB_STOCK_IMAGE_PREFIX
                        + bookClubDTO.getImage().getFileName()))
                .build()));
    }
}
