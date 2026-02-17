package com.hostfully.bookingapi.exception;

public class OverlapException extends RuntimeException {

    public OverlapException(String message) {
        super(message);
    }
}
