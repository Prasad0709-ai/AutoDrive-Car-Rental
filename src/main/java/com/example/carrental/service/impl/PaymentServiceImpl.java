package com.example.carrental.service.impl;

import com.example.carrental.dto.PaymentRequestDto;
import com.example.carrental.dto.PaymentResultDto;
import com.example.carrental.entity.*;
import com.example.carrental.exception.PaymentException;
import com.example.carrental.exception.ResourceNotFoundException;
import com.example.carrental.repository.BookingRepository;
import com.example.carrental.repository.PaymentRepository;
import com.example.carrental.service.PaymentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;

    @Value("${payment.provider:MOCK}")
    private String paymentProvider;

    @Value("${payment.key-id:rzp_test_mock_key}")
    private String keyId;

    @Value("${payment.key-secret:rzp_mock_secret}")
    private String keySecret;

    @Value("${payment.currency:USD}")
    private String defaultCurrency;

    public PaymentServiceImpl(PaymentRepository paymentRepository, BookingRepository bookingRepository) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public PaymentResultDto processPayment(PaymentRequestDto paymentRequest) {
        Booking booking = bookingRepository.findById(paymentRequest.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + paymentRequest.getBookingId()));

        if (booking.getPaymentStatus() == PaymentStatus.PAID) {
            return PaymentResultDto.success(
                    "ALREADY_PAID",
                    booking.getPayment() != null ? booking.getPayment().getTransactionId() : "TXN_EXISTING",
                    "/invoice/" + booking.getId()
            );
        }

        Payment payment = paymentRepository.findByBookingId(booking.getId())
                .orElse(new Payment(booking, booking.getTotalAmount(), defaultCurrency, paymentRequest.getPaymentMethod()));

        payment.setAmount(booking.getTotalAmount());
        payment.setCurrency(defaultCurrency);
        payment.setPaymentMethod(paymentRequest.getPaymentMethod() != null ? paymentRequest.getPaymentMethod() : "SANDBOX_GATEWAY");

        // Simulate Failure if requested via Sandbox test parameter
        if ("FAILED".equalsIgnoreCase(paymentRequest.getTestOutcome())) {
            payment.setStatus(PaymentTransactionStatus.FAILED);
            payment.setTransactionId("FAIL-" + System.currentTimeMillis());
            payment.setPaymentId("PAY-ERR-" + UUID.randomUUID().toString().substring(0, 8));
            paymentRepository.save(payment);

            booking.setPaymentStatus(PaymentStatus.PENDING);
            bookingRepository.save(booking);

            return PaymentResultDto.failure("Payment was declined by the issuer (Sandbox Simulation). Please verify your card details or try another payment method.");
        }

        // Process Success
        String txnId = "TXN-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 9000 + 1000);
        String payId = "PAY-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        String receiptUrl = "/invoice/" + booking.getId();

        payment.setStatus(PaymentTransactionStatus.SUCCESS);
        payment.setTransactionId(txnId);
        payment.setPaymentId(payId);
        payment.setReceiptUrl(receiptUrl);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        booking.setPaymentStatus(PaymentStatus.PAID);
        if (booking.getStatus() == BookingStatus.PENDING) {
            booking.setStatus(BookingStatus.CONFIRMED);
        }
        booking.setPaymentMethod(payment.getPaymentMethod());
        bookingRepository.save(booking);

        return PaymentResultDto.success(payId, txnId, receiptUrl);
    }

    @Override
    @Transactional(readOnly = true)
    public Payment findByBookingId(Long bookingId) {
        return paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found for booking id: " + bookingId));
    }

    @Override
    @Transactional(readOnly = true)
    public Payment findByPaymentId(String paymentId) {
        return paymentRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found for payment id: " + paymentId));
    }

    @Override
    @Transactional(readOnly = true)
    public Payment findByTransactionId(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found for transaction id: " + transactionId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payment> findAllPayments() {
        return paymentRepository.findAll();
    }

    @Override
    public boolean processWebhook(String payload, String signature) {
        // Webhook verification architecture
        if (signature == null || signature.isBlank()) {
            throw new PaymentException("Missing webhook signature header");
        }
        // In real Razorpay/Stripe, verify HMAC SHA256 of payload with keySecret.
        // For development/mock mode, we accept valid non-empty signatures.
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public long countSuccessfulPayments() {
        return paymentRepository.countByStatus(PaymentTransactionStatus.SUCCESS);
    }

    @Override
    @Transactional(readOnly = true)
    public Double getTotalRevenue() {
        return paymentRepository.sumSuccessfulPayments();
    }
}
