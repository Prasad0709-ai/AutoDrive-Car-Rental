package com.example.carrental.service.impl;

import com.example.carrental.dto.DashboardStatsDto;
import com.example.carrental.entity.BookingStatus;
import com.example.carrental.entity.VehicleStatus;
import com.example.carrental.repository.BookingRepository;
import com.example.carrental.repository.PaymentRepository;
import com.example.carrental.repository.UserRepository;
import com.example.carrental.repository.VehicleRepository;
import com.example.carrental.service.DashboardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final VehicleRepository vehicleRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;

    public DashboardServiceImpl(VehicleRepository vehicleRepository,
                                BookingRepository bookingRepository,
                                UserRepository userRepository,
                                PaymentRepository paymentRepository) {
        this.vehicleRepository = vehicleRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.paymentRepository = paymentRepository;
    }

    @Override
    public DashboardStatsDto getAdminDashboardStats() {
        DashboardStatsDto stats = new DashboardStatsDto();

        stats.setTotalVehicles(vehicleRepository.count());
        stats.setAvailableVehicles(vehicleRepository.countByStatus(VehicleStatus.AVAILABLE));
        stats.setRentedVehicles(vehicleRepository.countByStatus(VehicleStatus.RENTED));
        stats.setMaintenanceVehicles(vehicleRepository.countByStatus(VehicleStatus.MAINTENANCE));

        stats.setTotalUsers(userRepository.count());

        stats.setTotalBookings(bookingRepository.count());
        stats.setPendingBookings(bookingRepository.countByStatus(BookingStatus.PENDING));
        stats.setActiveBookings(bookingRepository.countByStatus(BookingStatus.ACTIVE));
        stats.setCompletedBookings(bookingRepository.countByStatus(BookingStatus.COMPLETED));
        stats.setCancelledBookings(bookingRepository.countByStatus(BookingStatus.CANCELLED));

        stats.setSuccessfulPayments(paymentRepository.count());
        stats.setTotalRevenue(paymentRepository.sumSuccessfulPayments());

        return stats;
    }
}
