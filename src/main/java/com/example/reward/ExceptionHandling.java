package com.example.reward;

import com.example.reward.exception.DomainException;
import com.example.reward.exception.NotFoundException;
import com.example.reward.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.validation.ConstraintViolationException;

@ControllerAdvice
public class ExceptionHandling {
    private static final Logger LOG = LoggerFactory.getLogger(ExceptionHandling.class);

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<String> handleDomainException(DomainException exception) {
        LOG.error("Error with message {} occured", exception.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(exception.getMessage());
    }

    @ExceptionHandler({ConstraintViolationException.class, MissingServletRequestParameterException.class,
                              ValidationException.class})
    public ResponseEntity<String> handleConstraintViolationException(Exception exception) {
        LOG.error("Error with message {} occured", exception.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(exception.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<String> handleNotFoundExceptions(NotFoundException exception) {
        LOG.error("Error with message {} occured", exception.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(exception.getMessage());
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, HttpMessageConversionException.class})
    public ResponseEntity<String> handleBindingExceptions(Exception exception) {
        LOG.error("Error with message {} occured", exception.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(exception.getMessage());
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<String> handleInternalExceptions(Exception exception) {
        LOG.error("Error with message {} occured", exception.getMessage());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(exception.getMessage());
    }
}
