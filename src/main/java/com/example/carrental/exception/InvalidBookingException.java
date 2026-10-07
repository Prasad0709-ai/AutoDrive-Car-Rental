package com.example.carrental.exception;

public class InvalidBookingException extends RuntimeException {
    public InvalidBookingException(String message) {
        super(message);
    }
}
