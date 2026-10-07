package com.example.carrental.controller;

import com.example.carrental.dto.ReviewDto;
import com.example.carrental.dto.UserProfileDto;
import com.example.carrental.entity.Booking;
import com.example.carrental.entity.BookingStatus;
import com.example.carrental.entity.Review;
import com.example.carrental.entity.User;
import com.example.carrental.security.CustomUserDetails;
import com.example.carrental.service.BookingService;
import com.example.carrental.service.InvoiceService;
import com.example.carrental.service.ReviewService;
import com.example.carrental.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class CustomerDashboardController {

    private final BookingService bookingService;
    private final UserService userService;
    private final ReviewService reviewService;
    private final InvoiceService invoiceService;

    public CustomerDashboardController(BookingService bookingService,
                                       UserService userService,
                                       ReviewService reviewService,
                                       InvoiceService invoiceService) {
        this.bookingService = bookingService;
        this.userService = userService;
        this.reviewService = reviewService;
        this.invoiceService = invoiceService;
    }

    @GetMapping("/dashboard")
    public String customerDashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Long userId = userDetails.getId();
        User user = userService.findById(userId);
        List<Booking> userBookings = bookingService.findBookingsByUserId(userId);

        long totalBookings = userBookings.size();
        long activeBookings = userBookings.stream().filter(b -> b.getStatus() == BookingStatus.ACTIVE).count();
        long completedBookings = userBookings.stream().filter(b -> b.getStatus() == BookingStatus.COMPLETED).count();
        long pendingBookings = userBookings.stream().filter(b -> b.getStatus() == BookingStatus.PENDING).count();

        // Recent 5 bookings
        List<Booking> recentBookings = userBookings.stream().limit(5).toList();

        model.addAttribute("user", user);
        model.addAttribute("totalBookings", totalBookings);
        model.addAttribute("activeBookings", activeBookings);
        model.addAttribute("completedBookings", completedBookings);
        model.addAttribute("pendingBookings", pendingBookings);
        model.addAttribute("recentBookings", recentBookings);

        return "customer/dashboard";
    }

    @GetMapping("/my-bookings")
    public String myBookings(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        List<Booking> bookings = bookingService.findBookingsByUserId(userDetails.getId());
        model.addAttribute("bookings", bookings);
        return "customer/my-bookings";
    }

    @GetMapping("/my-bookings/{id}")
    public String myBookingDetails(@PathVariable("id") Long id,
                                   @AuthenticationPrincipal CustomUserDetails userDetails,
                                   Model model) {
        Booking booking = bookingService.findById(id);
        if (!booking.getUser().getId().equals(userDetails.getId()) && !userDetails.getRole().name().equals("ADMIN")) {
            return "redirect:/error/403";
        }

        boolean hasReviewed = reviewService.hasUserReviewedVehicle(userDetails.getId(), booking.getVehicle().getId());

        model.addAttribute("booking", booking);
        model.addAttribute("hasReviewed", hasReviewed);
        model.addAttribute("reviewDto", new ReviewDto(booking.getVehicle().getId(), booking.getId()));
        return "customer/booking-details";
    }

    @GetMapping("/profile")
    public String showProfile(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User user = userService.findById(userDetails.getId());

        UserProfileDto profileDto = new UserProfileDto();
        profileDto.setId(user.getId());
        profileDto.setUsername(user.getUsername());
        profileDto.setEmail(user.getEmail());
        profileDto.setFirstName(user.getFirstName());
        profileDto.setLastName(user.getLastName());
        profileDto.setPhone(user.getPhone());
        profileDto.setAddress(user.getAddress());
        profileDto.setDriversLicense(user.getDriversLicense());

        model.addAttribute("profileDto", profileDto);
        return "customer/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@Valid @ModelAttribute("profileDto") UserProfileDto profileDto,
                                BindingResult bindingResult,
                                @AuthenticationPrincipal CustomUserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "customer/profile";
        }

        try {
            userService.updateUserProfile(userDetails.getId(), profileDto);
            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/profile";
    }

    @PostMapping("/reviews/add")
    public String submitReview(@Valid @ModelAttribute("reviewDto") ReviewDto reviewDto,
                               BindingResult bindingResult,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid review submission. Please ensure rating is 1-5 and comment is not empty.");
            return "redirect:/my-bookings/" + (reviewDto.getBookingId() != null ? reviewDto.getBookingId() : "");
        }

        try {
            reviewService.createReview(userDetails.getId(), reviewDto);
            redirectAttributes.addFlashAttribute("successMessage", "Thank you! Your vehicle review and rating have been published.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/my-bookings/" + (reviewDto.getBookingId() != null ? reviewDto.getBookingId() : "");
    }

    @GetMapping("/invoice/{bookingId}")
    public String viewInvoice(@PathVariable("bookingId") Long bookingId,
                              @AuthenticationPrincipal CustomUserDetails userDetails,
                              Model model) {
        Booking booking = invoiceService.getInvoiceBooking(bookingId);
        if (!booking.getUser().getId().equals(userDetails.getId()) && !userDetails.getRole().name().equals("ADMIN")) {
            return "redirect:/error/403";
        }

        model.addAttribute("booking", booking);
        return "customer/invoice";
    }

    @GetMapping("/invoice/{bookingId}/download")
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable("bookingId") Long bookingId,
                                                  @AuthenticationPrincipal CustomUserDetails userDetails) {
        Booking booking = invoiceService.getInvoiceBooking(bookingId);
        if (!booking.getUser().getId().equals(userDetails.getId()) && !userDetails.getRole().name().equals("ADMIN")) {
            return ResponseEntity.status(403).build();
        }

        byte[] invoiceBytes = invoiceService.generateInvoicePdfOrHtml(bookingId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"invoice-" + booking.getBookingNumber() + ".html\"")
                .contentType(MediaType.TEXT_HTML)
                .body(invoiceBytes);
    }
}
