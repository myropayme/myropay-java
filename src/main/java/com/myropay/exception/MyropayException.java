package com.myropay.exception;

import java.util.Map;

public class MyropayException extends Exception {
    private final Integer httpStatus;
    private final Map<String, Object> response;

    public MyropayException(String message) {
        this(message, null, null);
    }

    public MyropayException(String message, Integer httpStatus, Map<String, Object> response) {
        super(message);
        this.httpStatus = httpStatus;
        this.response = response;
    }

    public Integer getHttpStatus() {
        return httpStatus;
    }

    public Map<String, Object> getResponse() {
        return response;
    }
}
