package com.example.carrental.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    @Column(name = "payment_id", length = 100)
    private String paymentId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false, length = 10)
    private String currency = "USD";

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentTransactionStatus status = PaymentTransactionStatus.PENDING;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    @Column(name = "receipt_url", length = 500)
    private String receiptUrl;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Payment() {
    }

    public Payment(Booking booking, Double amount, String currency, String paymentMethod) {
        this.booking = booking;
        this.amount = amount;
        this.currency = currency != null ? currency : "USD";
        this.paymentMethod = paymentMethod;
        this.status = PaymentTransactionStatus.PENDING;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.currency == null) {
            this.currency = "USD";
        }
        if (this.status == null) {
            this.status = PaymentTransactionStatus.PENDING;
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public PaymentTransactionStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentTransactionStatus status) {
        this.status = status;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getReceiptUrl() {
        return receiptUrl;
    }

    public void setReceiptUrl(String receiptUrl) {
        this.receiptUrl = receiptUrl;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
