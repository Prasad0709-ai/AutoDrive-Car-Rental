package com.example.carrental.repository;

import com.example.carrental.entity.Booking;
import com.example.carrental.entity.BookingStatus;
import com.example.carrental.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByBookingNumber(String bookingNumber);

    List<Booking> findByUserOrderByCreatedAtDesc(User user);

    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Booking> findByVehicleIdOrderByCreatedAtDesc(Long vehicleId);

    List<Booking> findByStatus(BookingStatus status);

    long countByStatus(BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.vehicle.id = :vehicleId " +
           "AND b.status IN :blockingStatuses " +
           "AND b.pickupDate <= :returnDate AND b.returnDate >= :pickupDate " +
           "AND (:excludeBookingId IS NULL OR b.id <> :excludeBookingId)")
    List<Booking> findConflictingBookings(
            @Param("vehicleId") Long vehicleId,
            @Param("pickupDate") LocalDate pickupDate,
            @Param("returnDate") LocalDate returnDate,
            @Param("blockingStatuses") Collection<BookingStatus> blockingStatuses,
            @Param("excludeBookingId") Long excludeBookingId
    );

    @Query("SELECT b FROM Booking b WHERE b.pickupDate >= :today AND b.status IN ('PENDING', 'CONFIRMED') ORDER BY b.pickupDate ASC")
    List<Booking> findUpcomingBookings(@Param("today") LocalDate today);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.user.id = :userId AND b.status = :status")
    long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") BookingStatus status);
}
