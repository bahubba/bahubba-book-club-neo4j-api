package com.bahubba.bahubbabookclub.util;

/** Constants for the API */
public interface APIConstants {
    /* BOOK CLUB CONSTANTS */
    String[] RESERVED_NAMES = {"create", "default"};

    /* ERROR MESSAGES */
    String USER_NOT_FOUND = "Not logged in or user not found";

    /* S3 CONSTANTS */
    String BOOK_CLUB_STOCK_IMAGE_PREFIX = "book-clubs/images/stock/";
    int BOOK_CLUB_IMAGE_URL_TIMEOUT_MINUTES = 10;
}
