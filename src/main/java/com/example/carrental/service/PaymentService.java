package com.example.carrental.service;

import com.example.carrental.dto.PaymentRequestDto;
import com.example.carrental.dto.PaymentResultDto;
import com.example.carrental.entity.Payment;

import java.util.List;

public interface PaymentService {

    PaymentResultDto processPayment(PaymentRequestDto paymentRequest);

    Payment findByBookingId(Long bookingId);

    Payment findByPaymentId(String paymentId);

    Payment findByTransactionId(String transactionId);

    List<Payment> findAllPayments();

    boolean processWebhook(String payload, String signature);

    long countSuccessfulPayments();

    Double getTotalRevenue();
}
