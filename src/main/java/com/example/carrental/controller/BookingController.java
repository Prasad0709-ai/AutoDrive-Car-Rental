package com.example.carrental.controller;

import com.example.carrental.dto.BookingRequestDto;
import com.example.carrental.entity.Booking;
import com.example.carrental.entity.Vehicle;
import com.example.carrental.security.CustomUserDetails;
import com.example.carrental.service.BookingService;
import com.example.carrental.service.VehicleService;
import com.example.carrental.util.DateUtils;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final VehicleService vehicleService;

    public BookingController(BookingService bookingService, VehicleService vehicleService) {
        this.bookingService = bookingService;
        this.vehicleService = vehicleService;
    }

    @GetMapping("/new")
    public String showBookingForm(@RequestParam("vehicleId") Long vehicleId,
                                  @RequestParam(value = "pickupDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate pickupDate,
                                  @RequestParam(value = "returnDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate returnDate,
                                  @RequestParam(value = "pickupLocation", defaultValue = "Main Downtown Hub") String pickupLocation,
                                  @RequestParam(value = "returnLocation", defaultValue = "Main Downtown Hub") String returnLocation,
                                  @AuthenticationPrincipal CustomUserDetails userDetails,
                                  Model model) {
        Vehicle vehicle = vehicleService.findById(vehicleId);

        if (pickupDate == null) {
            pickupDate = LocalDate.now().plusDays(1);
        }
        if (returnDate == null) {
            returnDate = LocalDate.now().plusDays(4);
        }

        long days = DateUtils.calculateRentalDays(pickupDate, returnDate);
        double totalAmount = bookingService.calculateTotalAmount(vehicle, pickupDate, returnDate);

        BookingRequestDto bookingRequest = new BookingRequestDto();
        bookingRequest.setVehicleId(vehicleId);
        bookingRequest.setPickupDate(pickupDate);
        bookingRequest.setReturnDate(returnDate);
        bookingRequest.setPickupLocation(pickupLocation);
        bookingRequest.setReturnLocation(returnLocation);

        model.addAttribute("vehicle", vehicle);
        model.addAttribute("bookingRequest", bookingRequest);
        model.addAttribute("rentalDays", days);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("user", userDetails.getUser());

        return "booking/checkout-summary";
    }

    @PostMapping
    public String createBooking(@Valid @ModelAttribute("bookingRequest") BookingRequestDto requestDto,
                                BindingResult bindingResult,
                                @AuthenticationPrincipal CustomUserDetails userDetails,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            Vehicle vehicle = vehicleService.findById(requestDto.getVehicleId());
            model.addAttribute("vehicle", vehicle);
            model.addAttribute("rentalDays", DateUtils.calculateRentalDays(requestDto.getPickupDate(), requestDto.getReturnDate()));
            model.addAttribute("totalAmount", bookingService.calculateTotalAmount(vehicle, requestDto.getPickupDate(), requestDto.getReturnDate()));
            model.addAttribute("user", userDetails.getUser());
            return "booking/checkout-summary";
        }

        Booking booking = bookingService.createBooking(userDetails.getId(), requestDto);
        return "redirect:/payment/" + booking.getId();
    }

    @GetMapping("/{id}")
    public String viewBooking(@PathVariable("id") Long id,
                              @AuthenticationPrincipal CustomUserDetails userDetails,
                              Model model) {
        Booking booking = bookingService.findById(id);

        // Security authorization check
        boolean isAdmin = userDetails.getRole().name().equals("ADMIN");
        if (!isAdmin && !booking.getUser().getId().equals(userDetails.getId())) {
            return "redirect:/error/403";
        }

        model.addAttribute("booking", booking);
        model.addAttribute("isCancellable", booking.isCancellable());
        return "booking/confirmation";
    }

    @PostMapping("/{id}/cancel")
    public String cancelBooking(@PathVariable("id") Long id,
                                @AuthenticationPrincipal CustomUserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        boolean isAdmin = userDetails.getRole().name().equals("ADMIN");
        try {
            bookingService.cancelBooking(id, userDetails.getId(), isAdmin);
            redirectAttributes.addFlashAttribute("successMessage", "Booking has been cancelled successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return isAdmin ? "redirect:/admin/bookings" : "redirect:/my-bookings";
    }
}
