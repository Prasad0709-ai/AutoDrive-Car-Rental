package com.example.carrental.service;

import com.example.carrental.dto.BookingRequestDto;
import com.example.carrental.entity.*;
import com.example.carrental.exception.BookingConflictException;
import com.example.carrental.exception.InvalidBookingException;
import com.example.carrental.exception.VehicleNotAvailableException;
import com.example.carrental.repository.BookingRepository;
import com.example.carrental.repository.UserRepository;
import com.example.carrental.repository.VehicleRepository;
import com.example.carrental.service.impl.BookingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private UserRepository userRepository;

    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        bookingService = new BookingServiceImpl(bookingRepository, vehicleRepository, userRepository);
    }

    @Test
    void testCreateBooking_Success() {
        User user = new User();
        user.setId(1L);
        user.setIsActive(true);

        Vehicle vehicle = new Vehicle();
        vehicle.setId(5L);
        vehicle.setBrand("Toyota");
        vehicle.setModel("Corolla");
        vehicle.setPricePerDay(50.0);
        vehicle.setSecurityDeposit(150.0);
        vehicle.setStatus(VehicleStatus.AVAILABLE);
        vehicle.setIsAvailable(true);

        LocalDate pickup = LocalDate.now().plusDays(2);
        LocalDate ret = LocalDate.now().plusDays(5); // 3 days

        BookingRequestDto request = new BookingRequestDto();
        request.setVehicleId(5L);
        request.setPickupDate(pickup);
        request.setReturnDate(ret);
        request.setPickupLocation("Downtown Hub");
        request.setReturnLocation("Downtown Hub");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(vehicleRepository.findById(5L)).thenReturn(Optional.of(vehicle));
        when(bookingRepository.findConflictingBookings(eq(5L), eq(pickup), eq(ret), any(), isNull()))
                .thenReturn(Collections.emptyList());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking b = invocation.getArgument(0);
            b.setId(100L);
            return b;
        });

        Booking result = bookingService.createBooking(1L, request);

        assertNotNull(result);
        assertNotNull(result.getBookingNumber());
        assertTrue(result.getBookingNumber().startsWith("BK-"));
        assertEquals(BookingStatus.PENDING, result.getStatus());
        assertEquals(PaymentStatus.PENDING, result.getPaymentStatus());
        // 3 days * $50 + $150 = $300
        assertEquals(300.0, result.getTotalAmount());
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    void testCreateBooking_ConflictDetectionThrowsException() {
        User user = new User();
        user.setId(1L);
        user.setIsActive(true);

        Vehicle vehicle = new Vehicle();
        vehicle.setId(5L);
        vehicle.setBrand("Toyota");
        vehicle.setModel("Corolla");
        vehicle.setStatus(VehicleStatus.AVAILABLE);
        vehicle.setIsAvailable(true);

        LocalDate pickup = LocalDate.now().plusDays(3);
        LocalDate ret = LocalDate.now().plusDays(7);

        BookingRequestDto request = new BookingRequestDto();
        request.setVehicleId(5L);
        request.setPickupDate(pickup);
        request.setReturnDate(ret);
        request.setPickupLocation("Airport");
        request.setReturnLocation("Airport");

        // Existing overlapping booking
        Booking conflictingBooking = new Booking();
        conflictingBooking.setId(99L);
        conflictingBooking.setStatus(BookingStatus.CONFIRMED);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(vehicleRepository.findById(5L)).thenReturn(Optional.of(vehicle));
        when(bookingRepository.findConflictingBookings(eq(5L), eq(pickup), eq(ret), any(), isNull()))
                .thenReturn(List.of(conflictingBooking));

        assertThrows(BookingConflictException.class, () -> bookingService.createBooking(1L, request));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void testCreateBooking_InvalidDateRangeThrowsException() {
        User user = new User();
        user.setId(1L);
        user.setIsActive(true);

        Vehicle vehicle = new Vehicle();
        vehicle.setId(5L);
        vehicle.setStatus(VehicleStatus.AVAILABLE);
        vehicle.setIsAvailable(true);

        BookingRequestDto request = new BookingRequestDto();
        request.setVehicleId(5L);
        request.setPickupDate(LocalDate.now().plusDays(5));
        request.setReturnDate(LocalDate.now().plusDays(2)); // Return before pickup!
        request.setPickupLocation("Airport");
        request.setReturnLocation("Airport");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(vehicleRepository.findById(5L)).thenReturn(Optional.of(vehicle));

        assertThrows(InvalidBookingException.class, () -> bookingService.createBooking(1L, request));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void testCancelBooking_Success() {
        User user = new User();
        user.setId(1L);

        Booking booking = new Booking();
        booking.setId(200L);
        booking.setUser(user);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setPaymentStatus(PaymentStatus.PAID);

        when(bookingRepository.findById(200L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        bookingService.cancelBooking(200L, 1L, false);

        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        assertEquals(PaymentStatus.REFUNDED, booking.getPaymentStatus());
        verify(bookingRepository, times(1)).save(booking);
    }

    @Test
    void testCancelBooking_CompletedRentalCannotBeCancelled() {
        User user = new User();
        user.setId(1L);

        Booking booking = new Booking();
        booking.setId(201L);
        booking.setUser(user);
        booking.setStatus(BookingStatus.COMPLETED);

        when(bookingRepository.findById(201L)).thenReturn(Optional.of(booking));

        assertThrows(InvalidBookingException.class, () -> bookingService.cancelBooking(201L, 1L, false));
        verify(bookingRepository, never()).save(booking);
    }
}
