package com.example.carrental.service;

import com.example.carrental.dto.ReviewDto;
import com.example.carrental.entity.Review;

import java.util.List;

public interface ReviewService {

    Review createReview(Long userId, ReviewDto reviewDto);

    List<Review> findReviewsByVehicle(Long vehicleId);

    List<Review> findReviewsByUserId(Long userId);

    boolean hasUserReviewedVehicle(Long userId, Long vehicleId);

    void deleteReview(Long reviewId);
}
