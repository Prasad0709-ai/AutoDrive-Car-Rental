package com.example.carrental.service.impl;

import com.example.carrental.dto.ReviewDto;
import com.example.carrental.entity.Review;
import com.example.carrental.entity.User;
import com.example.carrental.entity.Vehicle;
import com.example.carrental.exception.InvalidBookingException;
import com.example.carrental.exception.ResourceNotFoundException;
import com.example.carrental.repository.ReviewRepository;
import com.example.carrental.repository.UserRepository;
import com.example.carrental.repository.VehicleRepository;
import com.example.carrental.service.ReviewService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;

    public ReviewServiceImpl(ReviewRepository reviewRepository,
                             UserRepository userRepository,
                             VehicleRepository vehicleRepository) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    public Review createReview(Long userId, ReviewDto reviewDto) {
        if (reviewDto.getRating() == null || reviewDto.getRating() < 1 || reviewDto.getRating() > 5) {
            throw new InvalidBookingException("Rating must be an integer between 1 and 5 stars.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Vehicle vehicle = vehicleRepository.findById(reviewDto.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + reviewDto.getVehicleId()));

        Review review = new Review();
        review.setUser(user);
        review.setVehicle(vehicle);
        review.setRating(reviewDto.getRating());
        review.setComment(reviewDto.getComment() != null ? reviewDto.getComment().trim() : "");

        Review savedReview = reviewRepository.save(review);

        // Recalculate average vehicle rating and review count
        Double avgRating = reviewRepository.getAverageRatingForVehicle(vehicle.getId());
        long totalReviews = reviewRepository.countByVehicleId(vehicle.getId());

        vehicle.setRating(Math.round(avgRating * 10.0) / 10.0);
        vehicle.setTotalReviews((int) totalReviews);
        vehicleRepository.save(vehicle);

        return savedReview;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Review> findReviewsByVehicle(Long vehicleId) {
        return reviewRepository.findByVehicleIdOrderByCreatedAtDesc(vehicleId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Review> findReviewsByUserId(Long userId) {
        return reviewRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasUserReviewedVehicle(Long userId, Long vehicleId) {
        return reviewRepository.existsByUserIdAndVehicleId(userId, vehicleId);
    }

    @Override
    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));
        Vehicle vehicle = review.getVehicle();
        reviewRepository.delete(review);

        // Update stats
        Double avgRating = reviewRepository.getAverageRatingForVehicle(vehicle.getId());
        long totalReviews = reviewRepository.countByVehicleId(vehicle.getId());
        vehicle.setRating(avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0);
        vehicle.setTotalReviews((int) totalReviews);
        vehicleRepository.save(vehicle);
    }
}
