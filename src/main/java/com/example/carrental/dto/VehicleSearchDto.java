package com.example.carrental.dto;

import com.example.carrental.entity.FuelType;
import com.example.carrental.entity.Transmission;
import com.example.carrental.entity.VehicleCategory;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class VehicleSearchDto {

    private VehicleCategory category;
    private FuelType fuelType;
    private Transmission transmission;
    private Integer seats;
    private Double maxPrice;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate pickupDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate returnDate;

    private String pickupLocation;
    private String returnLocation;

    public VehicleSearchDto() {
    }

    public VehicleCategory getCategory() {
        return category;
    }

    public void setCategory(VehicleCategory category) {
        this.category = category;
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

    public Double getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(Double maxPrice) {
        this.maxPrice = maxPrice;
    }

    public LocalDate getPickupDate() {
        return pickupDate;
    }

    public void setPickupDate(LocalDate pickupDate) {
        this.pickupDate = pickupDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public String getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    public String getReturnLocation() {
        return returnLocation;
    }

    public void setReturnLocation(String returnLocation) {
        this.returnLocation = returnLocation;
    }
}
