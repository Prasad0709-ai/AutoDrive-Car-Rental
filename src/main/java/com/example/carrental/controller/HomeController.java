package com.example.carrental.controller;

import com.example.carrental.dto.VehicleSearchDto;
import com.example.carrental.entity.Vehicle;
import com.example.carrental.service.VehicleService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.List;

@Controller
public class HomeController {

    private final VehicleService vehicleService;

    public HomeController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @GetMapping("/")
    public String home(Model model) {
        List<Vehicle> availableVehicles = vehicleService.findAvailableVehicles();
        // Featured vehicles for homepage showcase
        List<Vehicle> featuredVehicles = availableVehicles.stream().limit(6).toList();

        model.addAttribute("featuredVehicles", featuredVehicles);
        VehicleSearchDto searchDto = new VehicleSearchDto();
        searchDto.setPickupDate(LocalDate.now().plusDays(1));
        searchDto.setReturnDate(LocalDate.now().plusDays(4));
        model.addAttribute("searchDto", searchDto);
        return "index";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }

    @GetMapping("/terms")
    public String terms() {
        return "terms";
    }
}
