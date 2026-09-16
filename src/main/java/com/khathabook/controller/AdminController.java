package com.khathabook.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Objects;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.LocalDate;

import com.khathabook.model.Retailer;
import com.khathabook.model.Product;
import com.khathabook.model.SupportTicket;
import com.khathabook.model.Customer;
import com.khathabook.model.Order;
import com.khathabook.model.Payment;
import com.khathabook.repository.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final RetailerRepository retailerRepository;
    private final ProductRepository productRepository;
    private final SupportTicketRepository ticketRepository;
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final BillRepository billRepository;
    private final com.khathabook.service.NotificationService notificationService;

    private static String adminPassword = "admin123";
    private static String currentOtp = null;
    private static LocalDateTime otpExpiry = null;

    public AdminController(RetailerRepository retailerRepository,
                           ProductRepository productRepository,
                           SupportTicketRepository ticketRepository,
                           CustomerRepository customerRepository,
                           OrderRepository orderRepository,
                           PaymentRepository paymentRepository,
                           BillRepository billRepository,
                           com.khathabook.service.NotificationService notificationService) {
        this.retailerRepository = retailerRepository;
        this.productRepository = productRepository;
        this.ticketRepository = ticketRepository;
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.billRepository = billRepository;
        this.notificationService = notificationService;
    }

    // ================= LOGIN =================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");

        if ("khathabook.noreply@gmail.com".equals(email) && adminPassword.equals(password)) {
            return ResponseEntity.ok(Map.of(
                "success", true,
                "token", "mock-admin-jwt-token"
            ));
        } else {
            return ResponseEntity.status(401).body(Map.of(
                "success", false,
                "message", "Invalid admin email or password"
            ));
        }
    }

    // ================= PASSWORD RESET =================
    @PostMapping("/reset-password/request")
    public ResponseEntity<?> requestReset(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        if (!"khathabook.noreply@gmail.com".equals(email)) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Invalid admin email address"
            ));
        }

        currentOtp = String.format("%06d", new java.util.Random().nextInt(999999));
        otpExpiry = LocalDateTime.now().plusMinutes(5);

        // Send OTP
        notificationService.sendOtpEmail(email, currentOtp);

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "OTP sent to your email"
        ));
    }

    @PostMapping("/reset-password/verify")
    public ResponseEntity<?> verifyResetOtp(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String otp = payload.get("otp");

        if (!"khathabook.noreply@gmail.com".equals(email)) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Invalid email"
            ));
        }

        if (currentOtp == null || !currentOtp.equals(otp) || otpExpiry == null || LocalDateTime.now().isAfter(otpExpiry)) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Invalid or expired OTP"
            ));
        }

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "OTP verified"
        ));
    }

    @PostMapping("/reset-password/update")
    public ResponseEntity<?> updatePassword(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String otp = payload.get("otp");
        String newPassword = payload.get("password");

        if (!"khathabook.noreply@gmail.com".equals(email)) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Invalid email"
            ));
        }

        if (currentOtp == null || !currentOtp.equals(otp) || otpExpiry == null || LocalDateTime.now().isAfter(otpExpiry)) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Session expired or invalid OTP"
            ));
        }

        // Update password
        adminPassword = newPassword;
        currentOtp = null; // Clear OTP
        otpExpiry = null;

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Password updated successfully"
        ));
    }

    // ================= STATS =================
    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        long retailers = retailerRepository.count();
        long products = productRepository.count();
        long tickets = ticketRepository.count();
        long customers = customerRepository.count();
        long orders = orderRepository.count();
        
        // Revenue & Today's Volume
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        double revenueToday = orderRepository.findAll().stream()
            .filter(o -> o.getOrderDate() != null && o.getOrderDate().isAfter(startOfDay) && "COMPLETED".equalsIgnoreCase(o.getStatus()))
            .mapToDouble(Order::getTotalAmount)
            .sum();
            
        long ordersToday = orderRepository.findAll().stream()
            .filter(o -> o.getOrderDate() != null && o.getOrderDate().isAfter(startOfDay))
            .count();

        return ResponseEntity.ok(Map.of(
            "totalRetailers", retailers,
            "totalProducts", products,
            "totalCustomers", customers,
            "totalOrders", orders,
            "revenueToday", revenueToday,
            "ordersToday", ordersToday,
            "openTickets", ticketRepository.findByStatus("OPEN").size()
        ));
    }

    // ================= CUSTOMERS =================
    @GetMapping("/customers")
    public ResponseEntity<List<com.khathabook.model.Customer>> getAllCustomers() {
        return ResponseEntity.ok(customerRepository.findAll());
    }

    // ================= ORDERS =================
    @GetMapping("/orders")
    public ResponseEntity<List<com.khathabook.model.Order>> getAllOrders() {
        return ResponseEntity.ok(orderRepository.findAll());
    }

    // ================= PAYMENTS =================
    @GetMapping("/payments")
    public ResponseEntity<List<com.khathabook.model.Payment>> getAllPayments() {
        return ResponseEntity.ok(paymentRepository.findAll());
    }

    // ================= FRAUD DETECTION =================
    @GetMapping("/fraud/suspicious")
    public ResponseEntity<?> getSuspiciousAccounts() {
        List<Retailer> allRetailers = retailerRepository.findAll();
        List<Map<String, Object>> suspicious = allRetailers.stream()
            .map(r -> {
                long totalOrders = orderRepository.findByRetailerIdOrderByOrderDateDesc(r.getId()).size();
                long cancelledOrders = orderRepository.findByRetailerIdAndStatus(r.getId(), "CANCELLED").size();
                double cancellationRate = totalOrders == 0 ? 0 : (double) cancelledOrders / totalOrders;
                
                if (cancellationRate > 0.2 || "SUSPENDED".equalsIgnoreCase(r.getApprovalStatus())) {
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("id", r.getId());
                    map.put("name", r.getName());
                    map.put("shopName", r.getShopName());
                    map.put("reason", cancellationRate > 0.2 ? "High Cancellation Rate (" + Math.round(cancellationRate * 100) + "%)" : "Suspended Account");
                    map.put("severity", "HIGH");
                    return map;
                }
                return null;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
            
        return ResponseEntity.ok(suspicious);
    }

    // ================= RETAILERS =================
    @GetMapping("/retailers")
    public ResponseEntity<List<Retailer>> getAllRetailers() {
        return ResponseEntity.ok(retailerRepository.findAll());
    }

    @PutMapping("/retailers/{id}/verify")
    public ResponseEntity<?> verifyRetailer(@PathVariable Long id, @RequestBody Map<String, String> status) {
        Optional<Retailer> opt = retailerRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        Retailer r = opt.get();
        String newStatus = status.get("status"); // APPROVED, REJECTED, SUSPENDED
        r.setApprovalStatus(newStatus);
        
        if ("APPROVED".equalsIgnoreCase(newStatus)) {
            r.setIsVerified(true);
        } else {
            r.setIsVerified(false);
        }
        
        retailerRepository.save(r);
        return ResponseEntity.ok(r);
    }

    // ================= PRODUCTS =================
    @GetMapping("/products/pending")
    public ResponseEntity<List<Product>> getPendingProducts(HttpServletRequest request) {
        List<Product> products = productRepository.findByApprovalStatus("PENDING");
        products.forEach(p -> sanitizeProduct(p, request));
        return ResponseEntity.ok(products);
    }

    private void sanitizeProduct(Product p, HttpServletRequest request) {
        if (p.getImageUrl() != null && p.getImageUrl().contains("localhost:")) {
            String sanitized = p.getImageUrl().replaceAll("localhost:\\d+", request.getServerName() + ":" + request.getServerPort());
            p.setImageUrl(sanitized);
        }
    }

    @PutMapping("/products/{id}/moderate")
    public ResponseEntity<?> moderateProduct(@PathVariable Long id, @RequestBody Map<String, String> status) {
        Optional<Product> opt = productRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        Product p = opt.get();
        p.setApprovalStatus(status.get("status"));
        productRepository.save(p);
        return ResponseEntity.ok(p);
    }

    // ================= TICKETS =================
    @GetMapping("/tickets")
    public ResponseEntity<List<SupportTicket>> getTickets() {
        return ResponseEntity.ok(ticketRepository.findAll());
    }

    @PutMapping("/tickets/{id}/resolve")
    public ResponseEntity<?> resolveTicket(@PathVariable Long id, @RequestBody Map<String, String> data) {
        Optional<SupportTicket> opt = ticketRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        SupportTicket t = opt.get();
        t.setStatus(data.get("status"));
        t.setResolutionNotes(data.get("notes"));
        ticketRepository.save(t);
        return ResponseEntity.ok(t);
    }
}
