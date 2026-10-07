package com.example.carrental.service;

import com.example.carrental.dto.PaymentRequestDto;
import com.example.carrental.dto.PaymentResultDto;
import com.example.carrental.entity.*;
import com.example.carrental.repository.BookingRepository;
import com.example.carrental.repository.PaymentRepository;
import com.example.carrental.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BookingRepository bookingRepository;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentServiceImpl(paymentRepository, bookingRepository);
    }

    @Test
    void testProcessPayment_Success() {
        Booking booking = new Booking();
        booking.setId(10L);
        booking.setTotalAmount(285.0);
        booking.setStatus(BookingStatus.PENDING);
        booking.setPaymentStatus(PaymentStatus.PENDING);

        PaymentRequestDto request = new PaymentRequestDto(10L);
        request.setPaymentMethod("CREDIT_CARD");
        request.setTestOutcome("SUCCESS");

        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));
        when(paymentRepository.findByBookingId(10L)).thenReturn(Optional.empty());

        PaymentResultDto result = paymentService.processPayment(request);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getTransactionId());
        assertNotNull(result.getPaymentId());

        // Verify that booking was transitioned to CONFIRMED and PAID
        assertEquals(PaymentStatus.PAID, booking.getPaymentStatus());
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(bookingRepository, times(1)).save(booking);
    }

    @Test
    void testProcessPayment_FailureSimulation() {
        Booking booking = new Booking();
        booking.setId(11L);
        booking.setTotalAmount(150.0);
        booking.setStatus(BookingStatus.PENDING);
        booking.setPaymentStatus(PaymentStatus.PENDING);

        PaymentRequestDto request = new PaymentRequestDto(11L);
        request.setPaymentMethod("CREDIT_CARD");
        request.setTestOutcome("FAILED"); // Simulate issuer decline

        when(bookingRepository.findById(11L)).thenReturn(Optional.of(booking));
        when(paymentRepository.findByBookingId(11L)).thenReturn(Optional.empty());

        PaymentResultDto result = paymentService.processPayment(request);

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("declined"));

        // Booking remains PENDING
        assertEquals(PaymentStatus.PENDING, booking.getPaymentStatus());
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(bookingRepository, times(1)).save(booking);
    }
}
