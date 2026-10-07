package com.example.carrental.controller;

import com.example.carrental.dto.VehicleSearchDto;
import com.example.carrental.entity.Review;
import com.example.carrental.entity.Vehicle;
import com.example.carrental.service.ReviewService;
import com.example.carrental.service.VehicleService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;
    private final ReviewService reviewService;

    public VehicleController(VehicleService vehicleService, ReviewService reviewService) {
        this.vehicleService = vehicleService;
        this.reviewService = reviewService;
    }

    @GetMapping
    public String listVehicles(@ModelAttribute("searchDto") VehicleSearchDto searchDto, Model model) {
        List<Vehicle> vehicles = vehicleService.searchVehicles(searchDto);

        model.addAttribute("vehicles", vehicles);
        model.addAttribute("searchDto", searchDto);
        model.addAttribute("totalCount", vehicles.size());
        return "vehicles/catalog";
    }

    @GetMapping("/{id}")
    public String vehicleDetails(@PathVariable("id") Long id,
                                 @RequestParam(value = "pickupDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate pickupDate,
                                 @RequestParam(value = "returnDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate returnDate,
                                 Model model) {
        Vehicle vehicle = vehicleService.findById(id);
        List<Review> reviews = reviewService.findReviewsByVehicle(id);

        if (pickupDate == null) {
            pickupDate = LocalDate.now().plusDays(1);
        }
        if (returnDate == null) {
            returnDate = LocalDate.now().plusDays(4);
        }

        boolean isAvailable = vehicleService.isVehicleAvailableForDates(id, pickupDate, returnDate, null);

        model.addAttribute("vehicle", vehicle);
        model.addAttribute("reviews", reviews);
        model.addAttribute("pickupDate", pickupDate);
        model.addAttribute("returnDate", returnDate);
        model.addAttribute("isAvailableForDates", isAvailable);
        return "vehicles/details";
    }

    @GetMapping("/availability")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkAvailability(
            @RequestParam("vehicleId") Long vehicleId,
            @RequestParam("pickupDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate pickupDate,
            @RequestParam("returnDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate returnDate) {

        Map<String, Object> response = new HashMap<>();

        if (pickupDate.isBefore(LocalDate.now())) {
            response.put("available", false);
            response.put("message", "Pickup date cannot be in the past.");
            return ResponseEntity.ok(response);
        }

        if (returnDate.isBefore(pickupDate)) {
            response.put("available", false);
            response.put("message", "Return date cannot be earlier than pickup date.");
            return ResponseEntity.ok(response);
        }

        boolean available = vehicleService.isVehicleAvailableForDates(vehicleId, pickupDate, returnDate, null);
        response.put("available", available);
        response.put("message", available
                ? "Vehicle is available for the selected dates!"
                : "Vehicle is already booked or under maintenance for the selected date range.");

        return ResponseEntity.ok(response);
    }
}
