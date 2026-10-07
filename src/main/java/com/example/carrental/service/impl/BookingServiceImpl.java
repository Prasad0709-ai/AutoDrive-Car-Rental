package com.example.carrental.service.impl;

import com.example.carrental.dto.BookingRequestDto;
import com.example.carrental.entity.*;
import com.example.carrental.exception.BookingConflictException;
import com.example.carrental.exception.InvalidBookingException;
import com.example.carrental.exception.ResourceNotFoundException;
import com.example.carrental.exception.VehicleNotAvailableException;
import com.example.carrental.repository.BookingRepository;
import com.example.carrental.repository.UserRepository;
import com.example.carrental.repository.VehicleRepository;
import com.example.carrental.service.BookingService;
import com.example.carrental.util.BookingNumberGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

@Service
@Transactional
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;

    private static final List<BookingStatus> BLOCKING_STATUSES = Arrays.asList(
            BookingStatus.PENDING,
            BookingStatus.CONFIRMED,
            BookingStatus.ACTIVE
    );

    public BookingServiceImpl(BookingRepository bookingRepository,
                              VehicleRepository vehicleRepository,
                              UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.vehicleRepository = vehicleRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Booking createBooking(Long userId, BookingRequestDto requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new InvalidBookingException("User account is inactive. Please contact support.");
        }

        Vehicle vehicle = vehicleRepository.findById(requestDto.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + requestDto.getVehicleId()));

        if (vehicle.getStatus() == VehicleStatus.MAINTENANCE || !Boolean.TRUE.equals(vehicle.getIsAvailable())) {
            throw new VehicleNotAvailableException("Vehicle '" + vehicle.getDisplayName() + "' is currently under maintenance or unavailable for rent.");
        }

        LocalDate pickupDate = requestDto.getPickupDate();
        LocalDate returnDate = requestDto.getReturnDate();

        if (pickupDate == null || returnDate == null) {
            throw new InvalidBookingException("Both pickup and return dates are required.");
        }

        if (pickupDate.isBefore(LocalDate.now())) {
            throw new InvalidBookingException("Pickup date cannot be in the past.");
        }

        if (returnDate.isBefore(pickupDate)) {
            throw new InvalidBookingException("Return date cannot be earlier than pickup date.");
        }

        // Check for conflicting bookings
        List<Booking> conflicts = bookingRepository.findConflictingBookings(
                vehicle.getId(),
                pickupDate,
                returnDate,
                BLOCKING_STATUSES,
                null
        );

        if (!conflicts.isEmpty()) {
            throw new BookingConflictException("Vehicle '" + vehicle.getDisplayName() +
                    "' is already booked for the selected date range. Please select different dates or another vehicle.");
        }

        // Calculate rental pricing securely from backend database values
        double totalAmount = calculateTotalAmount(vehicle, pickupDate, returnDate);

        Booking booking = new Booking();
        booking.setBookingNumber(BookingNumberGenerator.generate());
        booking.setUser(user);
        booking.setVehicle(vehicle);
        booking.setPickupDate(pickupDate);
        booking.setReturnDate(returnDate);
        booking.setPickupLocation(requestDto.getPickupLocation().trim());
        booking.setReturnLocation(requestDto.getReturnLocation().trim());
        booking.setTotalAmount(totalAmount);
        booking.setStatus(BookingStatus.PENDING);
        booking.setPaymentStatus(PaymentStatus.PENDING);
        booking.setPaymentMethod(requestDto.getPaymentMethod());
        booking.setNotes(requestDto.getNotes());

        return bookingRepository.save(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public Booking findById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Booking findByBookingNumber(String bookingNumber) {
        return bookingRepository.findByBookingNumber(bookingNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with booking number: " + bookingNumber));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> findAllBookings() {
        return bookingRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> findBookingsByUserId(Long userId) {
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> findUpcomingBookings() {
        return bookingRepository.findUpcomingBookings(LocalDate.now());
    }

    @Override
    public void cancelBooking(Long bookingId, Long currentUserId, boolean isAdmin) {
        Booking booking = findById(bookingId);

        if (!isAdmin && !booking.getUser().getId().equals(currentUserId)) {
            throw new InvalidBookingException("You are not authorized to cancel this booking.");
        }

        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new InvalidBookingException("Completed rentals cannot be cancelled.");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new InvalidBookingException("This booking is already cancelled.");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        if (booking.getPaymentStatus() == PaymentStatus.PAID) {
            booking.setPaymentStatus(PaymentStatus.REFUNDED);
        }

        bookingRepository.save(booking);
    }

    @Override
    public void updateBookingStatus(Long bookingId, BookingStatus newStatus) {
        Booking booking = findById(bookingId);
        booking.setStatus(newStatus);
        bookingRepository.save(booking);
    }

    @Override
    public void updatePaymentStatus(Long bookingId, PaymentStatus paymentStatus) {
        Booking booking = findById(bookingId);
        booking.setPaymentStatus(paymentStatus);
        if (paymentStatus == PaymentStatus.PAID && booking.getStatus() == BookingStatus.PENDING) {
            booking.setStatus(BookingStatus.CONFIRMED);
        }
        bookingRepository.save(booking);
    }

    @Override
    public double calculateTotalAmount(Vehicle vehicle, LocalDate pickupDate, LocalDate returnDate) {
        long days = ChronoUnit.DAYS.between(pickupDate, returnDate);
        if (days <= 0) {
            days = 1; // Minimum 1 day rental
        }
        double rentalSubtotal = vehicle.getPricePerDay() * days;
        double deposit = vehicle.getSecurityDeposit() != null ? vehicle.getSecurityDeposit() : 0.0;
        return rentalSubtotal + deposit;
    }

    @Override
    @Transactional(readOnly = true)
    public long countBookingsByStatus(BookingStatus status) {
        return bookingRepository.countByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public long countUserBookingsByStatus(Long userId, BookingStatus status) {
        return bookingRepository.countByUserIdAndStatus(userId, status);
    }

    @Override
    @Transactional(readOnly = true)
    public long countUserTotalBookings(Long userId) {
        return bookingRepository.countByUserId(userId);
    }
}
