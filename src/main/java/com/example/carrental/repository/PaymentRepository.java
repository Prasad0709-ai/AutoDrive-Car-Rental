package com.example.carrental.repository;

import com.example.carrental.entity.Payment;
import com.example.carrental.entity.PaymentTransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByBookingId(Long bookingId);

    Optional<Payment> findByPaymentId(String paymentId);

    Optional<Payment> findByTransactionId(String transactionId);

    long countByStatus(PaymentTransactionStatus status);

    @Query("SELECT COALESCE(SUM(p.amount), 0.0) FROM Payment p WHERE p.status = 'SUCCESS'")
    Double sumSuccessfulPayments();
}
