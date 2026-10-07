package com.example.carrental.controller;

import com.example.carrental.dto.DashboardStatsDto;
import com.example.carrental.dto.MaintenanceDto;
import com.example.carrental.dto.VehicleDto;
import com.example.carrental.entity.*;
import com.example.carrental.service.*;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final DashboardService dashboardService;
    private final VehicleService vehicleService;
    private final BookingService bookingService;
    private final UserService userService;
    private final PaymentService paymentService;
    private final MaintenanceService maintenanceService;

    public AdminController(DashboardService dashboardService,
                           VehicleService vehicleService,
                           BookingService bookingService,
                           UserService userService,
                           PaymentService paymentService,
                           MaintenanceService maintenanceService) {
        this.dashboardService = dashboardService;
        this.vehicleService = vehicleService;
        this.bookingService = bookingService;
        this.userService = userService;
        this.paymentService = paymentService;
        this.maintenanceService = maintenanceService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        DashboardStatsDto stats = dashboardService.getAdminDashboardStats();
        List<Booking> recentBookings = bookingService.findAllBookings().stream().limit(8).toList();
        List<Maintenance> upcomingMaintenance = maintenanceService.findAll().stream().limit(5).toList();

        model.addAttribute("stats", stats);
        model.addAttribute("recentBookings", recentBookings);
        model.addAttribute("upcomingMaintenance", upcomingMaintenance);
        return "admin/dashboard";
    }

    // ==========================================
    // VEHICLE MANAGEMENT
    // ==========================================
    @GetMapping("/vehicles")
    public String manageVehicles(Model model) {
        List<Vehicle> vehicles = vehicleService.findAllVehicles();
        model.addAttribute("vehicles", vehicles);
        return "admin/vehicles";
    }

    @GetMapping("/vehicles/add")
    public String showAddVehicleForm(Model model) {
        if (!model.containsAttribute("vehicleDto")) {
            model.addAttribute("vehicleDto", new VehicleDto());
        }
        return "admin/vehicle-form";
    }

    @PostMapping("/vehicles/add")
    public String addVehicle(@Valid @ModelAttribute("vehicleDto") VehicleDto vehicleDto,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "admin/vehicle-form";
        }

        try {
            vehicleService.createVehicle(vehicleDto);
            redirectAttributes.addFlashAttribute("successMessage", "New vehicle added to fleet successfully.");
            return "redirect:/admin/vehicles";
        } catch (Exception ex) {
            bindingResult.reject("error", ex.getMessage());
            return "admin/vehicle-form";
        }
    }

    @GetMapping("/vehicles/edit/{id}")
    public String showEditVehicleForm(@PathVariable("id") Long id, Model model) {
        Vehicle vehicle = vehicleService.findById(id);

        VehicleDto dto = new VehicleDto();
        dto.setId(vehicle.getId());
        dto.setBrand(vehicle.getBrand());
        dto.setModel(vehicle.getModel());
        dto.setYear(vehicle.getYear());
        dto.setColor(vehicle.getColor());
        dto.setLicensePlate(vehicle.getLicensePlate());
        dto.setFuelType(vehicle.getFuelType());
        dto.setTransmission(vehicle.getTransmission());
        dto.setSeats(vehicle.getSeats());
        dto.setCategory(vehicle.getCategory());
        dto.setPricePerDay(vehicle.getPricePerDay());
        dto.setPricePerHour(vehicle.getPricePerHour());
        dto.setSecurityDeposit(vehicle.getSecurityDeposit());
        dto.setDescription(vehicle.getDescription());
        dto.setImageUrl(vehicle.getImageUrl());
        dto.setIsAvailable(vehicle.getIsAvailable());
        dto.setStatus(vehicle.getStatus());

        model.addAttribute("vehicleDto", dto);
        model.addAttribute("isEdit", true);
        return "admin/vehicle-form";
    }

    @PostMapping("/vehicles/edit/{id}")
    public String updateVehicle(@PathVariable("id") Long id,
                                @Valid @ModelAttribute("vehicleDto") VehicleDto vehicleDto,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", true);
            return "admin/vehicle-form";
        }

        try {
            vehicleService.updateVehicle(id, vehicleDto);
            redirectAttributes.addFlashAttribute("successMessage", "Vehicle specifications updated successfully.");
            return "redirect:/admin/vehicles";
        } catch (Exception ex) {
            bindingResult.reject("error", ex.getMessage());
            model.addAttribute("isEdit", true);
            return "admin/vehicle-form";
        }
    }

    @PostMapping("/vehicles/status/{id}")
    public String updateVehicleStatus(@PathVariable("id") Long id,
                                      @RequestParam("status") VehicleStatus status,
                                      RedirectAttributes redirectAttributes) {
        vehicleService.updateVehicleStatus(id, status);
        redirectAttributes.addFlashAttribute("successMessage", "Vehicle status set to: " + status.name());
        return "redirect:/admin/vehicles";
    }

    @PostMapping("/vehicles/toggle/{id}")
    public String toggleVehicleAvailability(@PathVariable("id") Long id,
                                            RedirectAttributes redirectAttributes) {
        vehicleService.toggleVehicleAvailability(id);
        redirectAttributes.addFlashAttribute("successMessage", "Vehicle availability toggled.");
        return "redirect:/admin/vehicles";
    }

    @PostMapping("/vehicles/delete/{id}")
    public String deleteVehicle(@PathVariable("id") Long id,
                                RedirectAttributes redirectAttributes) {
        try {
            vehicleService.deleteVehicle(id);
            redirectAttributes.addFlashAttribute("successMessage", "Vehicle permanently removed from fleet.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cannot delete vehicle with active booking records.");
        }
        return "redirect:/admin/vehicles";
    }

    // ==========================================
    // BOOKING MANAGEMENT
    // ==========================================
    @GetMapping("/bookings")
    public String manageBookings(Model model) {
        List<Booking> bookings = bookingService.findAllBookings();
        model.addAttribute("bookings", bookings);
        return "admin/bookings";
    }

    @PostMapping("/bookings/status/{id}")
    public String updateBookingStatus(@PathVariable("id") Long id,
                                      @RequestParam("status") BookingStatus status,
                                      RedirectAttributes redirectAttributes) {
        bookingService.updateBookingStatus(id, status);
        redirectAttributes.addFlashAttribute("successMessage", "Booking status updated to: " + status.name());
        return "redirect:/admin/bookings";
    }

    // ==========================================
    // USER MANAGEMENT
    // ==========================================
    @GetMapping("/users")
    public String manageUsers(Model model) {
        List<User> users = userService.findAllUsers();
        model.addAttribute("users", users);
        return "admin/users";
    }

    @PostMapping("/users/toggle/{id}")
    public String toggleUserStatus(@PathVariable("id") Long id,
                                   RedirectAttributes redirectAttributes) {
        userService.toggleUserStatus(id);
        redirectAttributes.addFlashAttribute("successMessage", "User account access status updated.");
        return "redirect:/admin/users";
    }

    // ==========================================
    // PAYMENT MANAGEMENT
    // ==========================================
    @GetMapping("/payments")
    public String managePayments(Model model) {
        List<Payment> payments = paymentService.findAllPayments();
        Double totalRevenue = paymentService.getTotalRevenue();
        model.addAttribute("payments", payments);
        model.addAttribute("totalRevenue", totalRevenue != null ? totalRevenue : 0.0);
        return "admin/payments";
    }

    // ==========================================
    // MAINTENANCE MANAGEMENT
    // ==========================================
    @GetMapping("/maintenance")
    public String manageMaintenance(Model model) {
        List<Maintenance> maintenanceList = maintenanceService.findAll();
        List<Vehicle> vehicles = vehicleService.findAllVehicles();

        model.addAttribute("maintenanceList", maintenanceList);
        model.addAttribute("vehicles", vehicles);
        model.addAttribute("maintenanceDto", new MaintenanceDto());
        return "admin/maintenance";
    }

    @PostMapping("/maintenance/add")
    public String addMaintenance(@Valid @ModelAttribute("maintenanceDto") MaintenanceDto dto,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid maintenance details provided.");
            return "redirect:/admin/maintenance";
        }

        maintenanceService.createMaintenance(dto);
        redirectAttributes.addFlashAttribute("successMessage", "Vehicle maintenance record registered successfully.");
        return "redirect:/admin/maintenance";
    }

    @PostMapping("/maintenance/status/{id}")
    public String updateMaintenanceStatus(@PathVariable("id") Long id,
                                          @RequestParam("status") MaintenanceStatus status,
                                          RedirectAttributes redirectAttributes) {
        maintenanceService.updateStatus(id, status);
        redirectAttributes.addFlashAttribute("successMessage", "Maintenance status updated to: " + status.name());
        return "redirect:/admin/maintenance";
    }

    @PostMapping("/maintenance/delete/{id}")
    public String deleteMaintenance(@PathVariable("id") Long id,
                                    RedirectAttributes redirectAttributes) {
        maintenanceService.deleteMaintenance(id);
        redirectAttributes.addFlashAttribute("successMessage", "Maintenance record removed.");
        return "redirect:/admin/maintenance";
    }
}
