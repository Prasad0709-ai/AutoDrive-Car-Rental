package com.example.carrental.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "maintenance")
public class Maintenance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @NotNull(message = "Maintenance type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MaintenanceType type;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Maintenance date is required")
    @Column(name = "maintenance_date", nullable = false)
    private LocalDate maintenanceDate;

    @PositiveOrZero(message = "Cost must be zero or positive")
    private Double cost;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MaintenanceStatus status = MaintenanceStatus.SCHEDULED;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Maintenance() {
    }

    public Maintenance(Vehicle vehicle, MaintenanceType type, String description, LocalDate maintenanceDate, Double cost, MaintenanceStatus status) {
        this.vehicle = vehicle;
        this.type = type;
        this.description = description;
        this.maintenanceDate = maintenanceDate;
        this.cost = cost;
        this.status = status;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = MaintenanceStatus.SCHEDULED;
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
