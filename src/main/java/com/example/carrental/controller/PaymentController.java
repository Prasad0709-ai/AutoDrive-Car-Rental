package com.example.carrental.controller;

import com.example.carrental.dto.PaymentRequestDto;
import com.example.carrental.dto.PaymentResultDto;
import com.example.carrental.entity.Booking;
import com.example.carrental.security.CustomUserDetails;
import com.example.carrental.service.BookingService;
import com.example.carrental.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentService paymentService;
    private final BookingService bookingService;

    public PaymentController(PaymentService paymentService, BookingService bookingService) {
        this.paymentService = paymentService;
        this.bookingService = bookingService;
    }

    @GetMapping("/{bookingId}")
    public String showPaymentPage(@PathVariable("bookingId") Long bookingId,
                                  @AuthenticationPrincipal CustomUserDetails userDetails,
                                  Model model) {
        Booking booking = bookingService.findById(bookingId);

        boolean isAdmin = userDetails.getRole().name().equals("ADMIN");
        if (!isAdmin && !booking.getUser().getId().equals(userDetails.getId())) {
            return "redirect:/error/403";
        }

        PaymentRequestDto paymentRequest = new PaymentRequestDto(bookingId);
        model.addAttribute("booking", booking);
        model.addAttribute("paymentRequest", paymentRequest);
        return "booking/payment";
    }

    @PostMapping("/process")
    public String processPayment(@ModelAttribute("paymentRequest") PaymentRequestDto paymentRequest,
                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        Booking booking = bookingService.findById(paymentRequest.getBookingId());

        boolean isAdmin = userDetails.getRole().name().equals("ADMIN");
        if (!isAdmin && !booking.getUser().getId().equals(userDetails.getId())) {
            return "redirect:/error/403";
        }

        PaymentResultDto result = paymentService.processPayment(paymentRequest);

        if (result.isSuccess()) {
            redirectAttributes.addFlashAttribute("result", result);
            return "redirect:/payment/success?bookingId=" + booking.getId();
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", result.getMessage());
            return "redirect:/payment/failed?bookingId=" + booking.getId() + "&error=" + result.getMessage();
        }
    }

    @GetMapping("/success")
    public String paymentSuccess(@RequestParam("bookingId") Long bookingId,
                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                 Model model) {
        Booking booking = bookingService.findById(bookingId);
        boolean isAdmin = userDetails.getRole().name().equals("ADMIN");
        if (!isAdmin && !booking.getUser().getId().equals(userDetails.getId())) {
            return "redirect:/error/403";
        }

        model.addAttribute("booking", booking);
        return "booking/payment-success";
    }

    @GetMapping("/failed")
    public String paymentFailed(@RequestParam("bookingId") Long bookingId,
                                @RequestParam(value = "error", defaultValue = "Payment could not be authorized.") String error,
                                @AuthenticationPrincipal CustomUserDetails userDetails,
                                Model model) {
        Booking booking = bookingService.findById(bookingId);
        boolean isAdmin = userDetails.getRole().name().equals("ADMIN");
        if (!isAdmin && !booking.getUser().getId().equals(userDetails.getId())) {
            return "redirect:/error/403";
        }

        model.addAttribute("booking", booking);
        model.addAttribute("errorMessage", error);
        return "booking/payment-failure";
    }

    @PostMapping("/webhook")
    @ResponseBody
    public ResponseEntity<String> handleWebhook(@RequestBody String payload,
                                                @RequestHeader(value = "X-Signature", required = false) String signature) {
        boolean valid = paymentService.processWebhook(payload, signature);
        if (valid) {
            return ResponseEntity.ok("Webhook acknowledged");
        }
        return ResponseEntity.badRequest().body("Invalid signature");
    }
}
