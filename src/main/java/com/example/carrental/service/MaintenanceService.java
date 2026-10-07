package com.example.carrental.service;

import com.example.carrental.dto.MaintenanceDto;
import com.example.carrental.entity.Maintenance;
import com.example.carrental.entity.MaintenanceStatus;

import java.util.List;

public interface MaintenanceService {

    Maintenance createMaintenance(MaintenanceDto dto);

    Maintenance updateMaintenance(Long id, MaintenanceDto dto);

    Maintenance findById(Long id);

    List<Maintenance> findAll();

    List<Maintenance> findByVehicleId(Long vehicleId);

    void updateStatus(Long id, MaintenanceStatus status);

    void deleteMaintenance(Long id);

    long countByStatus(MaintenanceStatus status);
}
