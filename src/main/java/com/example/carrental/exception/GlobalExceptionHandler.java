package com.example.carrental.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ModelAndView handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("error/404");
        mav.addObject("errorMessage", ex.getMessage());
        mav.addObject("url", request.getRequestURI());
        return mav;
    }

    @ExceptionHandler(BookingConflictException.class)
    public ModelAndView handleBookingConflict(BookingConflictException ex, HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("error/booking-error");
        mav.addObject("errorTitle", "Booking Schedule Conflict");
        mav.addObject("errorMessage", ex.getMessage());
        mav.addObject("url", request.getRequestURI());
        return mav;
    }

    @ExceptionHandler(VehicleNotAvailableException.class)
    public ModelAndView handleVehicleNotAvailable(VehicleNotAvailableException ex, HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("error/booking-error");
        mav.addObject("errorTitle", "Vehicle Unavailable");
        mav.addObject("errorMessage", ex.getMessage());
        mav.addObject("url", request.getRequestURI());
        return mav;
    }

    @ExceptionHandler(InvalidBookingException.class)
    public ModelAndView handleInvalidBooking(InvalidBookingException ex, HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("error/booking-error");
        mav.addObject("errorTitle", "Invalid Booking Request");
        mav.addObject("errorMessage", ex.getMessage());
        mav.addObject("url", request.getRequestURI());
        return mav;
    }

    @ExceptionHandler(PaymentException.class)
    public ModelAndView handlePaymentException(PaymentException ex, HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("error/payment-error");
        mav.addObject("errorMessage", ex.getMessage());
        mav.addObject("url", request.getRequestURI());
        return mav;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ModelAndView handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("error/generic-error");
        mav.addObject("errorTitle", "Invalid Request");
        mav.addObject("errorMessage", ex.getMessage());
        mav.addObject("url", request.getRequestURI());
        return mav;
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleGeneralException(Exception ex, HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("error/500");
        mav.addObject("errorMessage", ex.getMessage() != null ? ex.getMessage() : "An unexpected server error occurred.");
        mav.addObject("url", request.getRequestURI());
        return mav;
    }
}
