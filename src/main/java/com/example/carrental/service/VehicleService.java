package com.example.carrental.service;

import com.example.carrental.dto.VehicleDto;
import com.example.carrental.dto.VehicleSearchDto;
import com.example.carrental.entity.Vehicle;
import com.example.carrental.entity.VehicleCategory;
import com.example.carrental.entity.VehicleStatus;

import java.time.LocalDate;
import java.util.List;

public interface VehicleService {

    Vehicle createVehicle(VehicleDto dto);

    Vehicle updateVehicle(Long id, VehicleDto dto);

    Vehicle findById(Long id);

    List<Vehicle> findAllVehicles();

    List<Vehicle> findAvailableVehicles();

    List<Vehicle> findByCategory(VehicleCategory category);

    List<Vehicle> searchVehicles(VehicleSearchDto searchDto);

    List<Vehicle> findAvailableVehiclesForDates(LocalDate pickupDate, LocalDate returnDate);

    boolean isVehicleAvailableForDates(Long vehicleId, LocalDate pickupDate, LocalDate returnDate, Long excludeBookingId);

    void updateVehicleStatus(Long id, VehicleStatus status);

    void toggleVehicleAvailability(Long id);

    void deleteVehicle(Long id);

    long countVehiclesByStatus(VehicleStatus status);

    long countTotalVehicles();
}
