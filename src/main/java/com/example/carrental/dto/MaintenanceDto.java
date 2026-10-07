package com.example.carrental.dto;

import com.example.carrental.entity.MaintenanceStatus;
import com.example.carrental.entity.MaintenanceType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class MaintenanceDto {

    private Long id;

    @NotNull(message = "Vehicle is required")
    private Long vehicleId;

    @NotNull(message = "Maintenance type is required")
    private MaintenanceType type;

    private String description;

    @NotNull(message = "Maintenance date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate maintenanceDate;

    @PositiveOrZero(message = "Cost must be zero or positive")
    private Double cost;

    @NotNull(message = "Status is required")
    private MaintenanceStatus status = MaintenanceStatus.SCHEDULED;

    public MaintenanceDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public MaintenanceType getType() {
        return type;
    }

    public void setType(MaintenanceType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getMaintenanceDate() {
        return maintenanceDate;
    }

    public void setMaintenanceDate(LocalDate maintenanceDate) {
        this.maintenanceDate = maintenanceDate;
    }

    public Double getCost() {
        return cost;
    }

    public void setCost(Double cost) {
        this.cost = cost;
    }

    public MaintenanceStatus getStatus() {
        return status;
    }

    public void setStatus(MaintenanceStatus status) {
        this.status = status;
    }
}
