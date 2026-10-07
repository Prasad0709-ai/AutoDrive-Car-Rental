package com.example.carrental.repository;

import com.example.carrental.entity.Maintenance;
import com.example.carrental.entity.MaintenanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaintenanceRepository extends JpaRepository<Maintenance, Long> {

    List<Maintenance> findByVehicleIdOrderByMaintenanceDateDesc(Long vehicleId);

    List<Maintenance> findByStatus(MaintenanceStatus status);

    long countByStatus(MaintenanceStatus status);
}
