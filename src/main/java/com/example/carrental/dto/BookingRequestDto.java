package com.example.carrental.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class BookingRequestDto {

    @NotNull(message = "Please select a vehicle")
    private Long vehicleId;

    @NotNull(message = "Pickup date is required")
    @FutureOrPresent(message = "Pickup date cannot be in the past")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate pickupDate;

    @NotNull(message = "Return date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate returnDate;

    @NotBlank(message = "Pickup location is required")
    private String pickupLocation;

    @NotBlank(message = "Return location is required")
    private String returnLocation;

    private String notes;
    private String paymentMethod = "MOCK_CARD";

    public BookingRequestDto() {
    }

    // Getters and Setters
    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public LocalDate getPickupDate() {
        return pickupDate;
    }

    public void setPickupDate(LocalDate pickupDate) {
        this.pickupDate = pickupDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public String getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    public String getReturnLocation() {
        return returnLocation;
    }

    public void setReturnLocation(String returnLocation) {
        this.returnLocation = returnLocation;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
