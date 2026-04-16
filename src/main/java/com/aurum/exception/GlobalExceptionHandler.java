package com.aurum.exception;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public Map<String, Object> handleException(RuntimeException ex) {

        return Map.of(
                "status", "error",
                "message", ex.getMessage()
        );
    }
}