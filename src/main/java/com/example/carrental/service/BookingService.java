package com.example.carrental.service;

import com.example.carrental.dto.BookingRequestDto;
import com.example.carrental.entity.Booking;
import com.example.carrental.entity.BookingStatus;
import com.example.carrental.entity.PaymentStatus;
import com.example.carrental.entity.Vehicle;

import java.time.LocalDate;
import java.util.List;

public interface BookingService {

    Booking createBooking(Long userId, BookingRequestDto requestDto);

    Booking findById(Long id);

    Booking findByBookingNumber(String bookingNumber);

    List<Booking> findAllBookings();

    List<Booking> findBookingsByUserId(Long userId);

    List<Booking> findUpcomingBookings();

    void cancelBooking(Long bookingId, Long currentUserId, boolean isAdmin);

    void updateBookingStatus(Long bookingId, BookingStatus newStatus);

    void updatePaymentStatus(Long bookingId, PaymentStatus paymentStatus);

    double calculateTotalAmount(Vehicle vehicle, LocalDate pickupDate, LocalDate returnDate);

    long countBookingsByStatus(BookingStatus status);

    long countUserBookingsByStatus(Long userId, BookingStatus status);

    long countUserTotalBookings(Long userId);
}
