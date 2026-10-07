package com.example.carrental.service;

import com.example.carrental.dto.ReviewDto;
import com.example.carrental.entity.Review;
import com.example.carrental.entity.User;
import com.example.carrental.entity.Vehicle;
import com.example.carrental.exception.InvalidBookingException;
import com.example.carrental.repository.ReviewRepository;
import com.example.carrental.repository.UserRepository;
import com.example.carrental.repository.VehicleRepository;
import com.example.carrental.service.impl.ReviewServiceImpl;
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
public class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    private ReviewService reviewService;

    @BeforeEach
    void setUp() {
        reviewService = new ReviewServiceImpl(reviewRepository, userRepository, vehicleRepository);
    }

    @Test
    void testCreateReview_Success() {
        User user = new User();
        user.setId(1L);

        Vehicle vehicle = new Vehicle();
        vehicle.setId(5L);
        vehicle.setRating(4.0);
        vehicle.setTotalReviews(1);

        ReviewDto dto = new ReviewDto();
        dto.setVehicleId(5L);
        dto.setRating(5);
        dto.setComment("Outstanding performance and clean interior!");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(vehicleRepository.findById(5L)).thenReturn(Optional.of(vehicle));
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(reviewRepository.getAverageRatingForVehicle(5L)).thenReturn(4.5);
        when(reviewRepository.countByVehicleId(5L)).thenReturn(2L);

        Review result = reviewService.createReview(1L, dto);

        assertNotNull(result);
        assertEquals(5, result.getRating());
        assertEquals("Outstanding performance and clean interior!", result.getComment());

        // Verify vehicle statistics were updated
        assertEquals(4.5, vehicle.getRating());
        assertEquals(2, vehicle.getTotalReviews());
        verify(vehicleRepository, times(1)).save(vehicle);
    }

    @Test
    void testCreateReview_InvalidRatingThrowsException() {
        ReviewDto dto = new ReviewDto();
        dto.setVehicleId(5L);
        dto.setRating(6); // Invalid rating > 5
        dto.setComment("Too high rating");

        assertThrows(InvalidBookingException.class, () -> reviewService.createReview(1L, dto));
        verify(reviewRepository, never()).save(any(Review.class));
    }
}
