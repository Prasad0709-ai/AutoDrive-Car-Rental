package com.example.carrental.service;

import com.example.carrental.entity.Booking;

public interface InvoiceService {

    Booking getInvoiceBooking(Long bookingId);

    byte[] generateInvoicePdfOrHtml(Long bookingId);
}
