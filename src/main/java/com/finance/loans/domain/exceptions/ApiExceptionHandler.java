package com.finance.loans.domain.exceptions;

import java.time.ZonedDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(value = { NotFoundException.class })
    public ResponseEntity<Object> handleNotFoundException(NotFoundException e) {
        ApiException apiException = new ApiException(
            e.getMessage(),
            ZonedDateTime.now(),
            e.getHttpStatus());
        return new ResponseEntity<>(apiException, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = { BadRequestException.class })
    public ResponseEntity<Object> handleBadRequestException(BadRequestException e) {
        ApiException apiException = new ApiException(
            e.getMessage(),
            ZonedDateTime.now(),
            e.getHttpStatus());
        return new ResponseEntity<>(apiException, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleArgumentNotValidException(MethodArgumentNotValidException e) {
        ApiException apiException = new ApiException(
            e.getAllErrors().get(0).getDefaultMessage(),
            ZonedDateTime.now(),
            HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(apiException, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NumberFormatException.class)
    public ResponseEntity<Object> handleArgumentNotValidException(NumberFormatException e) {
        ApiException apiException = new ApiException(
            "All characters must be digits.",
            ZonedDateTime.now(),
            HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(apiException, HttpStatus.BAD_REQUEST);
    }
}
