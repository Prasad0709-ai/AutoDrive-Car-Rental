package com.example.carrental.service.impl;

import com.example.carrental.dto.VehicleDto;
import com.example.carrental.dto.VehicleSearchDto;
import com.example.carrental.entity.Booking;
import com.example.carrental.entity.BookingStatus;
import com.example.carrental.entity.Vehicle;
import com.example.carrental.entity.VehicleCategory;
import com.example.carrental.entity.VehicleStatus;
import com.example.carrental.exception.ResourceNotFoundException;
import com.example.carrental.repository.BookingRepository;
import com.example.carrental.repository.VehicleRepository;
import com.example.carrental.service.VehicleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Service
@Transactional
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final BookingRepository bookingRepository;

    private static final List<BookingStatus> BLOCKING_STATUSES = Arrays.asList(
            BookingStatus.PENDING,
            BookingStatus.CONFIRMED,
            BookingStatus.ACTIVE
    );

    public VehicleServiceImpl(VehicleRepository vehicleRepository, BookingRepository bookingRepository) {
        this.vehicleRepository = vehicleRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public Vehicle createVehicle(VehicleDto dto) {
        if (vehicleRepository.findByLicensePlate(dto.getLicensePlate()).isPresent()) {
            throw new IllegalArgumentException("License plate '" + dto.getLicensePlate() + "' already exists.");
        }

        Vehicle vehicle = new Vehicle();
        mapDtoToVehicle(dto, vehicle);
        return vehicleRepository.save(vehicle);
    }

    @Override
    public Vehicle updateVehicle(Long id, VehicleDto dto) {
        Vehicle vehicle = findById(id);

        vehicleRepository.findByLicensePlate(dto.getLicensePlate()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new IllegalArgumentException("License plate '" + dto.getLicensePlate() + "' is already in use by another vehicle.");
            }
        });

        mapDtoToVehicle(dto, vehicle);
        return vehicleRepository.save(vehicle);
    }

    private void mapDtoToVehicle(VehicleDto dto, Vehicle vehicle) {
        vehicle.setBrand(dto.getBrand().trim());
        vehicle.setModel(dto.getModel().trim());
        vehicle.setYear(dto.getYear());
        vehicle.setColor(dto.getColor());
        vehicle.setLicensePlate(dto.getLicensePlate().trim().toUpperCase());
        vehicle.setFuelType(dto.getFuelType());
        vehicle.setTransmission(dto.getTransmission());
        vehicle.setSeats(dto.getSeats());
        vehicle.setCategory(dto.getCategory());
        vehicle.setPricePerDay(dto.getPricePerDay());
        vehicle.setPricePerHour(dto.getPricePerHour());
        vehicle.setSecurityDeposit(dto.getSecurityDeposit() != null ? dto.getSecurityDeposit() : 0.0);
        vehicle.setDescription(dto.getDescription());
        vehicle.setImageUrl(dto.getImageUrl());
        if (dto.getIsAvailable() != null) {
            vehicle.setIsAvailable(dto.getIsAvailable());
        }
        if (dto.getStatus() != null) {
            vehicle.setStatus(dto.getStatus());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Vehicle findById(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vehicle> findAllVehicles() {
        return vehicleRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vehicle> findAvailableVehicles() {
        return vehicleRepository.findByIsAvailableTrueAndStatus(VehicleStatus.AVAILABLE);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vehicle> findByCategory(VehicleCategory category) {
        return vehicleRepository.findByCategory(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vehicle> searchVehicles(VehicleSearchDto searchDto) {
        if (searchDto == null) {
            return findAvailableVehicles();
        }

        // If dates are provided, start with vehicles available for that date window
        List<Vehicle> candidateVehicles;
        if (searchDto.getPickupDate() != null && searchDto.getReturnDate() != null) {
            candidateVehicles = findAvailableVehiclesForDates(searchDto.getPickupDate(), searchDto.getReturnDate());
        } else {
            candidateVehicles = vehicleRepository.searchVehicles(
                    searchDto.getCategory(),
                    searchDto.getFuelType(),
                    searchDto.getTransmission(),
                    searchDto.getSeats(),
                    searchDto.getMaxPrice(),
                    null
            );
        }

        // Apply filters on candidate vehicles
        return candidateVehicles.stream()
                .filter(v -> searchDto.getCategory() == null || v.getCategory() == searchDto.getCategory())
                .filter(v -> searchDto.getFuelType() == null || v.getFuelType() == searchDto.getFuelType())
                .filter(v -> searchDto.getTransmission() == null || v.getTransmission() == searchDto.getTransmission())
                .filter(v -> searchDto.getSeats() == null || v.getSeats() >= searchDto.getSeats())
                .filter(v -> searchDto.getMaxPrice() == null || v.getPricePerDay() <= searchDto.getMaxPrice())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vehicle> findAvailableVehiclesForDates(LocalDate pickupDate, LocalDate returnDate) {
        if (pickupDate == null || returnDate == null || returnDate.isBefore(pickupDate)) {
            return findAvailableVehicles();
        }
        return vehicleRepository.findAvailableVehiclesForDateRange(pickupDate, returnDate);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isVehicleAvailableForDates(Long vehicleId, LocalDate pickupDate, LocalDate returnDate, Long excludeBookingId) {
        Vehicle vehicle = findById(vehicleId);

        // A vehicle is not available if it is in MAINTENANCE or manually flagged unavailable
        if (vehicle.getStatus() == VehicleStatus.MAINTENANCE || !Boolean.TRUE.equals(vehicle.getIsAvailable())) {
            return false;
        }

        if (pickupDate == null || returnDate == null || returnDate.isBefore(pickupDate)) {
            return false;
        }

        List<Booking> conflicts = bookingRepository.findConflictingBookings(
                vehicleId,
                pickupDate,
                returnDate,
                BLOCKING_STATUSES,
                excludeBookingId
        );

        return conflicts.isEmpty();
    }

    @Override
    public void updateVehicleStatus(Long id, VehicleStatus status) {
        Vehicle vehicle = findById(id);
        vehicle.setStatus(status);
        if (status == VehicleStatus.MAINTENANCE || status == VehicleStatus.RENTED) {
            vehicle.setIsAvailable(false);
        } else if (status == VehicleStatus.AVAILABLE) {
            vehicle.setIsAvailable(true);
        }
        vehicleRepository.save(vehicle);
    }

    @Override
    public void toggleVehicleAvailability(Long id) {
        Vehicle vehicle = findById(id);
        vehicle.setIsAvailable(!vehicle.getIsAvailable());
        if (!vehicle.getIsAvailable() && vehicle.getStatus() == VehicleStatus.AVAILABLE) {
            vehicle.setStatus(VehicleStatus.MAINTENANCE);
        } else if (vehicle.getIsAvailable() && vehicle.getStatus() == VehicleStatus.MAINTENANCE) {
            vehicle.setStatus(VehicleStatus.AVAILABLE);
        }
        vehicleRepository.save(vehicle);
    }

    @Override
    public void deleteVehicle(Long id) {
        Vehicle vehicle = findById(id);
        vehicleRepository.delete(vehicle);
    }

    @Override
    @Transactional(readOnly = true)
    public long countVehiclesByStatus(VehicleStatus status) {
        return vehicleRepository.countByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public long countTotalVehicles() {
        return vehicleRepository.count();
    }
}
