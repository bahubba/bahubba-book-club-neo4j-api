package com.bahubba.bahubbabookclub.exception;

import com.bahubba.bahubbabookclub.model.dto.ResponseWrapperDTO;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/** Global exception handler to return proper HTTP status codes and possibly data */
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler
    public ResponseEntity<String> handleBadBookClubActionException(BadBookClubActionException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<String> handleBookClubNotFoundException(BookClubNotFoundException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<String> handleMembershipNotFoundException(MembershipNotFoundException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<String> handleMembershipRequestNotFoundException(MembershipRequestNotFoundException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<ResponseWrapperDTO<Page<?>>> handlePageSizeTooLargeException(PageSizeTooLargeException e) {
        return new ResponseEntity<>(new ResponseWrapperDTO<>(e.getMessage(), e.getPayload()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<ResponseWrapperDTO<Page<?>>> handlePageSizeTooSmallException(PageSizeTooSmallException e) {
        return new ResponseEntity<>(new ResponseWrapperDTO<>(e.getMessage(), e.getPayload()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<String> handleUserNotFoundException(UserNotFoundException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<String> handleTokenRefreshException(TokenRefreshException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler
    public ResponseEntity<String> handleUnauthorizedBookClubActionException(UnauthorizedBookClubActionException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
    }
}
