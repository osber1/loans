package com.finance.interest.exception;

import java.time.ZonedDateTime;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class ApiException {

    private final ZonedDateTime timestamp;

    private final HttpStatus status;

    private final String message;

    public ApiException(String message, ZonedDateTime timestamp, HttpStatus status) {
        this.timestamp = timestamp;
        this.status = status;
        this.message = message;
    }
}
