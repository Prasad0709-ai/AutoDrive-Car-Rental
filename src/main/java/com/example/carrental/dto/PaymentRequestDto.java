package com.example.carrental.dto;

import jakarta.validation.constraints.NotNull;

public class PaymentRequestDto {

    @NotNull(message = "Booking ID is required")
    private Long bookingId;

    private String paymentMethod = "MOCK_CARD";

    // "SUCCESS" or "FAILED" for sandbox demonstration
    private String testOutcome = "SUCCESS";

    private String cardHolder;
    private String cardNumber;
    private String expiryDate;
    private String cvv;

    public PaymentRequestDto() {
    }

    public PaymentRequestDto(Long bookingId) {
        this.bookingId = bookingId;
    }

    // Getters and Setters
    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getTestOutcome() {
        return testOutcome;
    }

    public void setTestOutcome(String testOutcome) {
        this.testOutcome = testOutcome;
    }

    public String getCardHolder() {
        return cardHolder;
    }

    public void setCardHolder(String cardHolder) {
        this.cardHolder = cardHolder;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }
}
