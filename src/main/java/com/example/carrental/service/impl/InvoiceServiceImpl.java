package com.example.carrental.service.impl;

import com.example.carrental.entity.Booking;
import com.example.carrental.entity.Payment;
import com.example.carrental.entity.User;
import com.example.carrental.entity.Vehicle;
import com.example.carrental.exception.ResourceNotFoundException;
import com.example.carrental.repository.BookingRepository;
import com.example.carrental.service.InvoiceService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

@Service
@Transactional(readOnly = true)
public class InvoiceServiceImpl implements InvoiceService {

    private final BookingRepository bookingRepository;

    @Value("${carrental.app.name:AutoDrive Car Rental}")
    private String appName;

    @Value("${carrental.app.contact-email:support@autodrive.com}")
    private String contactEmail;

    @Value("${carrental.app.contact-phone:+1 (800) 555-0199}")
    private String contactPhone;

    public InvoiceServiceImpl(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Override
    public Booking getInvoiceBooking(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));
    }

    @Override
    public byte[] generateInvoicePdfOrHtml(Long bookingId) {
        Booking booking = getInvoiceBooking(bookingId);
        User user = booking.getUser();
        Vehicle vehicle = booking.getVehicle();
        Payment payment = booking.getPayment();

        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        DateTimeFormatter dtFmt = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

        long rentalDays = booking.getRentalDays();
        double dailyRate = vehicle.getPricePerDay();
        double rentalSubtotal = dailyRate * rentalDays;
        double deposit = vehicle.getSecurityDeposit() != null ? vehicle.getSecurityDeposit() : 0.0;
        double total = booking.getTotalAmount();

        String txnId = payment != null && payment.getTransactionId() != null ? payment.getTransactionId() : "N/A";
        String paidDate = payment != null && payment.getPaidAt() != null ? payment.getPaidAt().format(dtFmt) : "Pending";
        String payStatus = booking.getPaymentStatus().name();

        String html = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <title>Invoice - %s</title>
                    <style>
                        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 40px; color: #2d3748; background: #fff; }
                        .invoice-card { max-width: 800px; margin: 0 auto; border: 1px solid #e2e8f0; border-radius: 12px; padding: 40px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1); }
                        .header { display: flex; justify-content: space-between; align-items: flex-start; border-bottom: 2px solid #edf2f7; padding-bottom: 24px; margin-bottom: 24px; }
                        .logo { font-size: 24px; font-weight: 800; color: #2563eb; letter-spacing: -0.5px; }
                        .badge { display: inline-block; padding: 4px 12px; border-radius: 9999px; font-size: 12px; font-weight: 700; text-transform: uppercase; }
                        .badge-paid { background: #dcfce7; color: #15803d; }
                        .badge-pending { background: #fef9c3; color: #a16207; }
                        .badge-refunded { background: #fee2e2; color: #b91c1c; }
                        .details-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 24px; margin-bottom: 32px; font-size: 14px; line-height: 1.6; }
                        table { width: 100%; border-collapse: collapse; margin-bottom: 32px; font-size: 14px; }
                        th { background: #f8fafc; text-align: left; padding: 12px; font-weight: 600; color: #475569; border-bottom: 2px solid #e2e8f0; }
                        td { padding: 12px; border-bottom: 1px solid #e2e8f0; }
                        .total-row td { font-weight: 700; font-size: 16px; color: #0f172a; border-top: 2px solid #0f172a; }
                        .footer { margin-top: 40px; text-align: center; font-size: 12px; color: #94a3b8; border-top: 1px solid #edf2f7; padding-top: 20px; }
                    </style>
                </head>
                <body>
                    <div class="invoice-card">
                        <div class="header">
                            <div>
                                <div class="logo">%s</div>
                                <p style="margin: 4px 0 0; color: #64748b; font-size: 13px;">Official Rental Receipt & Invoice</p>
                                <p style="margin: 2px 0 0; color: #64748b; font-size: 13px;">%s | %s</p>
                            </div>
                            <div style="text-align: right;">
                                <h3 style="margin: 0 0 6px; color: #0f172a;">INVOICE</h3>
                                <p style="margin: 0; font-size: 14px; font-weight: 600; color: #2563eb;">#%s</p>
                                <div style="margin-top: 8px;">
                                    <span class="badge %s">%s</span>
                                </div>
                            </div>
                        </div>

                        <div class="details-grid">
                            <div>
                                <strong style="color: #64748b; text-transform: uppercase; font-size: 11px;">Billed To:</strong><br>
                                <strong style="font-size: 15px;">%s</strong><br>
                                Email: %s<br>
                                Phone: %s<br>
                                Address: %s
                            </div>
                            <div>
                                <strong style="color: #64748b; text-transform: uppercase; font-size: 11px;">Payment & Booking Details:</strong><br>
                                Transaction ID: <code>%s</code><br>
                                Paid On: %s<br>
                                Pickup: <strong>%s</strong> (%s)<br>
                                Return: <strong>%s</strong> (%s)
                            </div>
                        </div>

                        <table>
                            <thead>
                                <tr>
                                    <th>Description</th>
                                    <th>Duration / Rate</th>
                                    <th style="text-align: right;">Amount (USD)</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr>
                                    <td>
                                        <strong>%s</strong><br>
                                        <span style="color: #64748b; font-size: 13px;">License Plate: %s | Category: %s</span>
                                    </td>
                                    <td>%d Days @ $%s/day</td>
                                    <td style="text-align: right;">$%s</td>
                                </tr>
                                <tr>
                                    <td>
                                        <strong>Refundable Security Deposit</strong><br>
                                        <span style="color: #64748b; font-size: 13px;">Held and returned upon vehicle inspection</span>
                                    </td>
                                    <td>1 Deposit</td>
                                    <td style="text-align: right;">$%s</td>
                                </tr>
                                <tr class="total-row">
                                    <td colspan="2" style="text-align: right;">Total Paid:</td>
                                    <td style="text-align: right;">$%s</td>
                                </tr>
                            </tbody>
                        </table>

                        <div class="footer">
                            <p>Thank you for choosing %s. For 24/7 roadside assistance or rental inquiries, contact us at %s.</p>
                            <p>This invoice is electronically generated and valid without a physical signature.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(
                booking.getBookingNumber(),
                appName,
                contactEmail,
                contactPhone,
                booking.getBookingNumber(),
                "PAID".equals(payStatus) ? "badge-paid" : "REFUNDED".equals(payStatus) ? "badge-refunded" : "badge-pending",
                payStatus,
                user.getFullName(),
                user.getEmail(),
                user.getPhone() != null ? user.getPhone() : "N/A",
                user.getAddress() != null ? user.getAddress() : "N/A",
                txnId,
                paidDate,
                booking.getPickupDate().format(dateFmt),
                booking.getPickupLocation(),
                booking.getReturnDate().format(dateFmt),
                booking.getReturnLocation(),
                vehicle.getDisplayName(),
                vehicle.getLicensePlate(),
                vehicle.getCategory().name(),
                rentalDays,
                String.format("%.2f", dailyRate),
                String.format("%.2f", rentalSubtotal),
                String.format("%.2f", deposit),
                String.format("%.2f", total),
                appName,
                contactEmail
        );

        return html.getBytes(StandardCharsets.UTF_8);
    }
}
