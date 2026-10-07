package com.example.carrental.repository;

import com.example.carrental.entity.FuelType;
import com.example.carrental.entity.Transmission;
import com.example.carrental.entity.Vehicle;
import com.example.carrental.entity.VehicleCategory;
import com.example.carrental.entity.VehicleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    List<Vehicle> findByIsAvailableTrueAndStatus(VehicleStatus status);

    List<Vehicle> findByCategory(VehicleCategory category);

    List<Vehicle> findByStatus(VehicleStatus status);

    long countByStatus(VehicleStatus status);

    Optional<Vehicle> findByLicensePlate(String licensePlate);

    @Query("SELECT v FROM Vehicle v WHERE " +
           "(:category IS NULL OR v.category = :category) AND " +
           "(:fuelType IS NULL OR v.fuelType = :fuelType) AND " +
           "(:transmission IS NULL OR v.transmission = :transmission) AND " +
           "(:minSeats IS NULL OR v.seats >= :minSeats) AND " +
           "(:maxPrice IS NULL OR v.pricePerDay <= :maxPrice) AND " +
           "(:status IS NULL OR v.status = :status)")
    List<Vehicle> searchVehicles(
            @Param("category") VehicleCategory category,
            @Param("fuelType") FuelType fuelType,
            @Param("transmission") Transmission transmission,
            @Param("minSeats") Integer minSeats,
            @Param("maxPrice") Double maxPrice,
            @Param("status") VehicleStatus status
    );

    @Query("SELECT v FROM Vehicle v WHERE v.status = 'AVAILABLE' AND v.isAvailable = true AND v.id NOT IN (" +
           "SELECT b.vehicle.id FROM Booking b WHERE b.status IN ('PENDING', 'CONFIRMED', 'ACTIVE') AND " +
           "b.pickupDate <= :returnDate AND b.returnDate >= :pickupDate" +
           ")")
    List<Vehicle> findAvailableVehiclesForDateRange(
            @Param("pickupDate") LocalDate pickupDate,
            @Param("returnDate") LocalDate returnDate
    );
}
