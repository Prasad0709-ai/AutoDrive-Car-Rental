package com.example.carrental.exception;

public class VehicleNotAvailableException extends RuntimeException {
    public VehicleNotAvailableException(String message) {
        super(message);
    }
}
