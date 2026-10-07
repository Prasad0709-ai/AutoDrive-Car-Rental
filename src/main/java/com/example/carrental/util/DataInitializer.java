package com.example.carrental.util;

import com.example.carrental.entity.*;
import com.example.carrental.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;
    private final MaintenanceRepository maintenanceRepository;
    private final PaymentRepository paymentRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           VehicleRepository vehicleRepository,
                           BookingRepository bookingRepository,
                           ReviewRepository reviewRepository,
                           MaintenanceRepository maintenanceRepository,
                           PaymentRepository paymentRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.vehicleRepository = vehicleRepository;
        this.bookingRepository = bookingRepository;
        this.reviewRepository = reviewRepository;
        this.maintenanceRepository = maintenanceRepository;
        this.paymentRepository = paymentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        seedUsers();
        seedVehicles();
        seedBookingsAndReviews();
    }

    private void seedUsers() {
        if (userRepository.count() == 0) {
            // Seed Admin
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@autodrive.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFirstName("System");
            admin.setLastName("Admin");
            admin.setPhone("+1 (800) 555-0100");
            admin.setAddress("100 Corporate Parkway, Suite 500, New York, NY");
            admin.setDriversLicense("DL-ADM-998822");
            admin.setRole(Role.ADMIN);
            admin.setIsActive(true);
            userRepository.save(admin);

            // Seed Customers
            User customer1 = new User();
            customer1.setUsername("customer");
            customer1.setEmail("john.doe@example.com");
            customer1.setPassword(passwordEncoder.encode("customer123"));
            customer1.setFirstName("John");
            customer1.setLastName("Doe");
            customer1.setPhone("+1 (555) 234-5678");
            customer1.setAddress("742 Evergreen Terrace, Springfield, OR");
            customer1.setDriversLicense("DL-OR-55443311");
            customer1.setRole(Role.CUSTOMER);
            customer1.setIsActive(true);
            userRepository.save(customer1);

            User customer2 = new User();
            customer2.setUsername("sarah");
            customer2.setEmail("sarah.connor@example.com");
            customer2.setPassword(passwordEncoder.encode("customer123"));
            customer2.setFirstName("Sarah");
            customer2.setLastName("Connor");
            customer2.setPhone("+1 (555) 987-6543");
            customer2.setAddress("42 Ocean Boulevard, Santa Monica, CA");
            customer2.setDriversLicense("DL-CA-99332211");
            customer2.setRole(Role.CUSTOMER);
            customer2.setIsActive(true);
            userRepository.save(customer2);
        }
    }

    private void seedVehicles() {
        if (vehicleRepository.count() == 0) {
            List<Vehicle> vehicles = Arrays.asList(
                createVehicle("Toyota", "Corolla", 2023, "Silver", "NY-COR-2301",
                        FuelType.PETROL, Transmission.AUTOMATIC, 5, VehicleCategory.ECONOMY,
                        45.0, 7.5, 150.0,
                        "Ultra-reliable and fuel-efficient daily driver featuring modern infotainment, lane assist, and great trunk space.",
                        "https://images.unsplash.com/photo-1623869675781-80aa31012a5a?auto=format&fit=crop&w=800&q=80",
                        true, VehicleStatus.AVAILABLE, 4.8, 12),

                createVehicle("Honda", "Civic", 2024, "Crystal Black", "NY-CIV-2402",
                        FuelType.PETROL, Transmission.AUTOMATIC, 5, VehicleCategory.ECONOMY,
                        50.0, 8.0, 150.0,
                        "Sporty compact sedan delivering dynamic handling, Apple CarPlay/Android Auto, and top-tier safety marks.",
                        "https://images.unsplash.com/photo-1590362891991-f776e747a588?auto=format&fit=crop&w=800&q=80",
                        true, VehicleStatus.AVAILABLE, 4.7, 9),

                createVehicle("Volkswagen", "Golf TSI", 2023, "Pacific Blue", "NJ-GLF-2303",
                        FuelType.PETROL, Transmission.MANUAL, 5, VehicleCategory.COMPACT,
                        55.0, 9.0, 200.0,
                        "Iconic hatchback with responsive turbo punch, European engineering, and versatile split-folding rear seats.",
                        "https://images.unsplash.com/photo-1541899481282-d53bffe3c35d?auto=format&fit=crop&w=800&q=80",
                        true, VehicleStatus.AVAILABLE, 4.6, 8),

                createVehicle("Hyundai", "Elantra", 2024, "Fiery Red", "NJ-ELA-2404",
                        FuelType.PETROL, Transmission.AUTOMATIC, 5, VehicleCategory.COMPACT,
                        48.0, 8.0, 150.0,
                        "Striking geometric styling loaded with modern driver assists, digital cockpit, and outstanding highway MPG.",
                        "https://images.unsplash.com/photo-1609521263047-f8f205293f24?auto=format&fit=crop&w=800&q=80",
                        true, VehicleStatus.AVAILABLE, 4.5, 6),

                createVehicle("Toyota", "RAV4 Hybrid", 2024, "Pearl White", "CA-RAV-2405",
                        FuelType.PETROL, Transmission.AUTOMATIC, 5, VehicleCategory.SUV,
                        75.0, 12.0, 250.0,
                        "All-wheel drive hybrid SUV offering spacious cabin room, rough-weather capability, and effortless fuel economy.",
                        "https://images.unsplash.com/photo-1568844293986-8d0400bd4745?auto=format&fit=crop&w=800&q=80",
                        true, VehicleStatus.AVAILABLE, 4.9, 18),

                createVehicle("Ford", "Explorer XLT", 2023, "Agate Black", "CA-EXP-2306",
                        FuelType.PETROL, Transmission.AUTOMATIC, 7, VehicleCategory.SUV,
                        88.0, 14.0, 300.0,
                        "Full-size 7-passenger family SUV with cavernous luggage room, tri-zone climate control, and powerful EcoBoost engine.",
                        "https://images.unsplash.com/photo-1533473359331-0135ef1b58bf?auto=format&fit=crop&w=800&q=80",
                        true, VehicleStatus.AVAILABLE, 4.7, 11),

                createVehicle("Tesla", "Model Y Long Range", 2024, "Midnight Silver", "CA-TSL-2407",
                        FuelType.ELECTRIC, Transmission.AUTOMATIC, 5, VehicleCategory.SUV,
                        98.0, 16.0, 350.0,
                        "100% electric crossover with instant torque, dual-motor AWD, panoramic glass roof, and access to Superchargers.",
                        "https://images.unsplash.com/photo-1560958089-b8a1929cea89?auto=format&fit=crop&w=800&q=80",
                        true, VehicleStatus.AVAILABLE, 4.9, 24),

                createVehicle("Mercedes-Benz", "E-Class AMG Line", 2024, "Obsidian Black", "FL-MBZ-2408",
                        FuelType.PETROL, Transmission.AUTOMATIC, 5, VehicleCategory.LUXURY,
                        145.0, 22.0, 500.0,
                        "Executive luxury sedan with Burmester surround sound, ambient multi-color lighting, and unmatched ride refinement.",
                        "https://images.unsplash.com/photo-1618843479313-40f8afb4b4d8?auto=format&fit=crop&w=800&q=80",
                        true, VehicleStatus.AVAILABLE, 5.0, 15),

                createVehicle("BMW", "5 Series 530i", 2024, "Alpine White", "FL-BMW-2409",
                        FuelType.PETROL, Transmission.AUTOMATIC, 5, VehicleCategory.LUXURY,
                        139.0, 21.0, 500.0,
                        "The pinnacle of executive athleticism. Features BMW Curved Display, sport seats, and smooth turbocharged delivery.",
                        "https://images.unsplash.com/photo-1555215695-3004980ad54e?auto=format&fit=crop&w=800&q=80",
                        true, VehicleStatus.AVAILABLE, 4.8, 14),

                createVehicle("Porsche", "Macan GTS", 2024, "Carmine Red", "FL-POR-2410",
                        FuelType.PETROL, Transmission.AUTOMATIC, 5, VehicleCategory.LUXURY,
                        189.0, 28.0, 600.0,
                        "Twin-turbo V6 performance SUV built for driving enthusiasts who refuse to compromise between speed and comfort.",
                        "https://images.unsplash.com/photo-1503376780353-7e6692767b70?auto=format&fit=crop&w=800&q=80",
                        true, VehicleStatus.AVAILABLE, 4.9, 19)
            );

            vehicleRepository.saveAll(vehicles);
        }
    }

    private Vehicle createVehicle(String brand, String model, int year, String color, String licensePlate,
                                  FuelType fuelType, Transmission transmission, int seats, VehicleCategory category,
                                  double pricePerDay, double pricePerHour, double securityDeposit,
                                  String description, String imageUrl, boolean available, VehicleStatus status,
                                  double rating, int reviews) {
        Vehicle v = new Vehicle();
        v.setBrand(brand);
        v.setModel(model);
        v.setYear(year);
        v.setColor(color);
        v.setLicensePlate(licensePlate);
        v.setFuelType(fuelType);
        v.setTransmission(transmission);
        v.setSeats(seats);
        v.setCategory(category);
        v.setPricePerDay(pricePerDay);
        v.setPricePerHour(pricePerHour);
        v.setSecurityDeposit(securityDeposit);
        v.setDescription(description);
        v.setImageUrl(imageUrl);
        v.setIsAvailable(available);
        v.setStatus(status);
        v.setRating(rating);
        v.setTotalReviews(reviews);
        return v;
    }

    private void seedBookingsAndReviews() {
        if (bookingRepository.count() == 0) {
            User customer = userRepository.findByUsername("customer").orElse(null);
            Vehicle vehicle = vehicleRepository.findAll().stream().findFirst().orElse(null);

            if (customer != null && vehicle != null) {
                // Completed historical booking
                Booking completedBooking = new Booking();
                completedBooking.setBookingNumber("BK-202609-10021");
                completedBooking.setUser(customer);
                completedBooking.setVehicle(vehicle);
                completedBooking.setPickupDate(LocalDate.now().minusDays(10));
                completedBooking.setReturnDate(LocalDate.now().minusDays(7));
                completedBooking.setPickupLocation("JFK International Airport, NY");
                completedBooking.setReturnLocation("JFK International Airport, NY");
                completedBooking.setTotalAmount(285.0);
                completedBooking.setStatus(BookingStatus.COMPLETED);
                completedBooking.setPaymentStatus(PaymentStatus.PAID);
                completedBooking.setPaymentMethod("CREDIT_CARD");
                completedBooking.setNotes("Client requested child safety seat.");
                completedBooking = bookingRepository.save(completedBooking);

                // Payment for completed booking
                Payment payment = new Payment(completedBooking, 285.0, "USD", "CREDIT_CARD");
                payment.setStatus(PaymentTransactionStatus.SUCCESS);
                payment.setPaymentId("PAY-SEED-001");
                payment.setTransactionId("TXN-SEED-998877");
                payment.setPaidAt(LocalDateTime.now().minusDays(10));
                payment.setReceiptUrl("/invoice/" + completedBooking.getId());
                paymentRepository.save(payment);

                // Seed review for the completed booking
                Review review = new Review();
                review.setUser(customer);
                review.setVehicle(vehicle);
                review.setRating(5);
                review.setComment("Vehicle was immaculate and pickup took less than 3 minutes. Truly five-star experience!");
                reviewRepository.save(review);

                // Maintenance sample for second vehicle
                List<Vehicle> allVehicles = vehicleRepository.findAll();
                if (allVehicles.size() > 1) {
                    Vehicle v2 = allVehicles.get(1);
                    Maintenance maintenance = new Maintenance();
                    maintenance.setVehicle(v2);
                    maintenance.setType(MaintenanceType.OIL_CHANGE);
                    maintenance.setDescription("Routine 15,000-mile synthetic oil change and multi-point safety inspection.");
                    maintenance.setMaintenanceDate(LocalDate.now().minusDays(3));
                    maintenance.setCost(89.50);
                    maintenance.setStatus(MaintenanceStatus.COMPLETED);
                    maintenanceRepository.save(maintenance);
                }
            }
        }
    }
}
