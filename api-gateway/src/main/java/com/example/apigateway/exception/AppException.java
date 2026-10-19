package com.example.apigateway.exception;

import lombok.Getter;

@Getter
public class AppException extends RuntimeException {
    private final int code;

    public AppException(int code, String message) {
        super(message);
        this.code = code;
    }

    public AppException(String message) {
        super(message);
        this.code = 400;
    }
}
