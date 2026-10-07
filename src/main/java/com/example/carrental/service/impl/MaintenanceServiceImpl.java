package com.example.carrental.service.impl;

import com.example.carrental.dto.MaintenanceDto;
import com.example.carrental.entity.Maintenance;
import com.example.carrental.entity.MaintenanceStatus;
import com.example.carrental.entity.Vehicle;
import com.example.carrental.entity.VehicleStatus;
import com.example.carrental.exception.ResourceNotFoundException;
import com.example.carrental.repository.MaintenanceRepository;
import com.example.carrental.repository.VehicleRepository;
import com.example.carrental.service.MaintenanceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class MaintenanceServiceImpl implements MaintenanceService {

    private final MaintenanceRepository maintenanceRepository;
    private final VehicleRepository vehicleRepository;

    public MaintenanceServiceImpl(MaintenanceRepository maintenanceRepository, VehicleRepository vehicleRepository) {
        this.maintenanceRepository = maintenanceRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    public Maintenance createMaintenance(MaintenanceDto dto) {
        Vehicle vehicle = vehicleRepository.findById(dto.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + dto.getVehicleId()));

        Maintenance maintenance = new Maintenance();
        maintenance.setVehicle(vehicle);
        maintenance.setType(dto.getType());
        maintenance.setDescription(dto.getDescription());
        maintenance.setMaintenanceDate(dto.getMaintenanceDate());
        maintenance.setCost(dto.getCost() != null ? dto.getCost() : 0.0);
        maintenance.setStatus(dto.getStatus());

        // If maintenance is active or scheduled, mark vehicle as under maintenance
        if (dto.getStatus() == MaintenanceStatus.IN_PROGRESS) {
            vehicle.setStatus(VehicleStatus.MAINTENANCE);
            vehicle.setIsAvailable(false);
            vehicleRepository.save(vehicle);
        }

        return maintenanceRepository.save(maintenance);
    }

    @Override
    public Maintenance updateMaintenance(Long id, MaintenanceDto dto) {
        Maintenance maintenance = findById(id);
        Vehicle vehicle = vehicleRepository.findById(dto.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + dto.getVehicleId()));

        maintenance.setVehicle(vehicle);
        maintenance.setType(dto.getType());
        maintenance.setDescription(dto.getDescription());
        maintenance.setMaintenanceDate(dto.getMaintenanceDate());
        maintenance.setCost(dto.getCost());
        maintenance.setStatus(dto.getStatus());

        if (dto.getStatus() == MaintenanceStatus.IN_PROGRESS) {
            vehicle.setStatus(VehicleStatus.MAINTENANCE);
            vehicle.setIsAvailable(false);
            vehicleRepository.save(vehicle);
        } else if (dto.getStatus() == MaintenanceStatus.COMPLETED && vehicle.getStatus() == VehicleStatus.MAINTENANCE) {
            vehicle.setStatus(VehicleStatus.AVAILABLE);
            vehicle.setIsAvailable(true);
            vehicleRepository.save(vehicle);
        }

        return maintenanceRepository.save(maintenance);
    }

    @Override
    @Transactional(readOnly = true)
    public Maintenance findById(Long id) {
        return maintenanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance record not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Maintenance> findAll() {
        return maintenanceRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Maintenance> findByVehicleId(Long vehicleId) {
        return maintenanceRepository.findByVehicleIdOrderByMaintenanceDateDesc(vehicleId);
    }

    @Override
    public void updateStatus(Long id, MaintenanceStatus status) {
        Maintenance maintenance = findById(id);
        maintenance.setStatus(status);

        Vehicle vehicle = maintenance.getVehicle();
        if (status == MaintenanceStatus.IN_PROGRESS) {
            vehicle.setStatus(VehicleStatus.MAINTENANCE);
            vehicle.setIsAvailable(false);
            vehicleRepository.save(vehicle);
        } else if (status == MaintenanceStatus.COMPLETED && vehicle.getStatus() == VehicleStatus.MAINTENANCE) {
            vehicle.setStatus(VehicleStatus.AVAILABLE);
            vehicle.setIsAvailable(true);
            vehicleRepository.save(vehicle);
        }

        maintenanceRepository.save(maintenance);
    }

    @Override
    public void deleteMaintenance(Long id) {
        Maintenance maintenance = findById(id);
        maintenanceRepository.delete(maintenance);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByStatus(MaintenanceStatus status) {
        return maintenanceRepository.countByStatus(status);
    }
}
