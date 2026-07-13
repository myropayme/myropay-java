package com.myropay.exception;

/**
 * Thrown before a request is even sent — e.g. no apiKey configured,
 * or a required parameter (amount, email) is missing.
 */
public class InvalidRequestException extends MyropayException {
    public InvalidRequestException(String message) {
        super(message);
    }
}
