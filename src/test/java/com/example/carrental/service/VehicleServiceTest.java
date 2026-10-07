package com.example.carrental.service;

import com.example.carrental.dto.VehicleDto;
import com.example.carrental.entity.*;
import com.example.carrental.repository.BookingRepository;
import com.example.carrental.repository.VehicleRepository;
import com.example.carrental.service.impl.VehicleServiceImpl;
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
public class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private BookingRepository bookingRepository;

    private VehicleService vehicleService;

    @BeforeEach
    void setUp() {
        vehicleService = new VehicleServiceImpl(vehicleRepository, bookingRepository);
    }

    @Test
    void testCreateVehicle_Success() {
        VehicleDto dto = new VehicleDto();
        dto.setBrand("Honda");
        dto.setModel("Civic");
        dto.setYear(2024);
        dto.setColor("Black");
        dto.setLicensePlate("TEST-CIV-01");
        dto.setCategory(VehicleCategory.ECONOMY);
        dto.setFuelType(FuelType.PETROL);
        dto.setTransmission(Transmission.AUTOMATIC);
        dto.setSeats(5);
        dto.setPricePerDay(50.0);
        dto.setSecurityDeposit(150.0);

        when(vehicleRepository.findByLicensePlate("TEST-CIV-01")).thenReturn(Optional.empty());
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> {
            Vehicle v = invocation.getArgument(0);
            v.setId(10L);
            return v;
        });

        Vehicle created = vehicleService.createVehicle(dto);

        assertNotNull(created);
        assertEquals("Honda", created.getBrand());
        assertEquals("TEST-CIV-01", created.getLicensePlate());
        assertEquals(VehicleStatus.AVAILABLE, created.getStatus());
        verify(vehicleRepository, times(1)).save(any(Vehicle.class));
    }

    @Test
    void testVehicleAvailability_NoConflict() {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(1L);
        vehicle.setStatus(VehicleStatus.AVAILABLE);
        vehicle.setIsAvailable(true);

        LocalDate pickup = LocalDate.now().plusDays(2);
        LocalDate ret = LocalDate.now().plusDays(5);

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(bookingRepository.findConflictingBookings(eq(1L), eq(pickup), eq(ret), any(), isNull()))
                .thenReturn(Collections.emptyList());

        boolean available = vehicleService.isVehicleAvailableForDates(1L, pickup, ret, null);

        assertTrue(available);
    }

    @Test
    void testVehicleAvailability_WhenUnderMaintenance() {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(2L);
        vehicle.setStatus(VehicleStatus.MAINTENANCE);
        vehicle.setIsAvailable(false);

        LocalDate pickup = LocalDate.now().plusDays(2);
        LocalDate ret = LocalDate.now().plusDays(5);

        when(vehicleRepository.findById(2L)).thenReturn(Optional.of(vehicle));

        boolean available = vehicleService.isVehicleAvailableForDates(2L, pickup, ret, null);

        assertFalse(available);
        verify(bookingRepository, never()).findConflictingBookings(any(), any(), any(), any(), any());
    }
}
