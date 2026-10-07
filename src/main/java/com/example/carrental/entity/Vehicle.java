package com.example.carrental.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vehicles")
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Brand is required")
    @Column(nullable = false, length = 50)
    private String brand;

    @NotBlank(message = "Model is required")
    @Column(nullable = false, length = 50)
    private String model;

    @Column(name = "model_year")
    private Integer year;

    @Column(length = 30)
    private String color;

    @NotBlank(message = "License plate is required")
    @Column(name = "license_plate", nullable = false, unique = true, length = 30)
    private String licensePlate;

    @NotNull(message = "Fuel type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "fuel_type", nullable = false, length = 20)
    private FuelType fuelType;

    @NotNull(message = "Transmission is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Transmission transmission;

    @NotNull(message = "Number of seats is required")
    @Positive(message = "Seats must be greater than zero")
    private Integer seats;

    @NotNull(message = "Category is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private VehicleCategory category;

    @NotNull(message = "Price per day is required")
    @Positive(message = "Price per day must be positive")
    @Column(name = "price_per_day", nullable = false)
    private Double pricePerDay;

    @Positive(message = "Price per hour must be positive")
    @Column(name = "price_per_hour")
    private Double pricePerHour;

    @PositiveOrZero(message = "Security deposit must be zero or positive")
    @Column(name = "security_deposit")
    private Double securityDeposit = 0.0;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = true;

    @NotNull(message = "Vehicle status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VehicleStatus status = VehicleStatus.AVAILABLE;

    @Column(nullable = false)
    private Double rating = 0.0;

    @Column(name = "total_reviews", nullable = false)
    private Integer totalReviews = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Booking> bookings = new ArrayList<>();

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Maintenance> maintenanceRecords = new ArrayList<>();

    public Vehicle() {
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.isAvailable == null) {
            this.isAvailable = true;
        }
        if (this.status == null) {
            this.status = VehicleStatus.AVAILABLE;
        }
        if (this.rating == null) {
            this.rating = 0.0;
        }
        if (this.totalReviews == null) {
            this.totalReviews = 0;
        }
        if (this.securityDeposit == null) {
            this.securityDeposit = 0.0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public String getDisplayName() {
        return brand + " " + model + " (" + (year != null ? year : "") + ")";
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public FuelType getFuelType() {
        return fuelType;
    }

    public void setFuelType(FuelType fuelType) {
        this.fuelType = fuelType;
    }

    public Transmission getTransmission() {
        return transmission;
    }

    public void setTransmission(Transmission transmission) {
        this.transmission = transmission;
    }

    public Integer getSeats() {
        return seats;
    }

    public void setSeats(Integer seats) {
        this.seats = seats;
    }

    public VehicleCategory getCategory() {
        return category;
    }

    public void setCategory(VehicleCategory category) {
        this.category = category;
    }

    public Double getPricePerDay() {
        return pricePerDay;
    }

    public void setPricePerDay(Double pricePerDay) {
        this.pricePerDay = pricePerDay;
    }

    public Double getPricePerHour() {
        return pricePerHour;
    }

    public void setPricePerHour(Double pricePerHour) {
        this.pricePerHour = pricePerHour;
    }

    public Double getSecurityDeposit() {
        return securityDeposit;
    }

    public void setSecurityDeposit(Double securityDeposit) {
        this.securityDeposit = securityDeposit;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Boolean getIsAvailable() {
        return isAvailable;
    }

    public void setIsAvailable(Boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    public VehicleStatus getStatus() {
        return status;
    }

    public void setStatus(VehicleStatus status) {
        this.status = status;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Integer getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(Integer totalReviews) {
        this.totalReviews = totalReviews;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<Booking> getBookings() {
        return bookings;
    }

    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    public List<Maintenance> getMaintenanceRecords() {
        return maintenanceRecords;
    }

    public void setMaintenanceRecords(List<Maintenance> maintenanceRecords) {
        this.maintenanceRecords = maintenanceRecords;
    }
}
