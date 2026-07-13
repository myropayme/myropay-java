package com.myropay.exception;

import java.util.Map;

/** Thrown when the MyroPay API itself returns an error response. */
public class ApiException extends MyropayException {
    public ApiException(String message, Integer httpStatus, Map<String, Object> response) {
        super(message, httpStatus, response);
    }
}
