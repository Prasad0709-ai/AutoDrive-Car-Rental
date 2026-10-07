package com.example.carrental.exception;

public class BookingConflictException extends RuntimeException {
    public BookingConflictException(String message) {
        super(message);
    }
}
