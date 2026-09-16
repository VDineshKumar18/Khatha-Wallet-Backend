package com.khathabook.controller;

import com.khathabook.model.Customer;
import com.khathabook.model.Otp;
import com.khathabook.model.Retailer;
import com.khathabook.repository.CustomerRepository;
import com.khathabook.repository.OtpRepository;
import com.khathabook.repository.RetailerRepository;
import com.khathabook.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/customer-auth")
@CrossOrigin(origins = "*")
public class CustomerAuthController {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OtpRepository otpRepository;
    
    @Autowired
    private RetailerRepository retailerRepository;

    @Autowired
    private NotificationService notificationService;

    // ================= PHONE LOGIN (Legacy/Backup) =================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> payload) {
        String phone = payload.get("phone");
        if (phone == null || phone.isEmpty()) return ResponseEntity.badRequest().body("Phone number is required");

        List<Customer> customers = customerRepository.findByPhone(phone);
        if (customers.isEmpty()) return ResponseEntity.status(404).body("No accounts found with this phone number");

        return ResponseEntity.ok(formatResponse(customers));
    }

    // ================= EMAIL OTP LOGIN (Unified) =================
    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestParam String email) {
        // ✅ CHECK IF USER EXISTS
        List<Customer> customers = customerRepository.findByEmail(email);
        if (customers.isEmpty()) {
            return ResponseEntity.status(404).body(Collections.singletonMap("message", "Email not registered. Please sign up."));
        }
        
        // Generate OTP
        String otp = String.valueOf(100000 + new Random().nextInt(900000));

        // Save OTP
        otpRepository.findByEmail(email).ifPresent(otpRepository::delete);
        Otp entity = new Otp();
        entity.setEmail(email);
        entity.setOtp(otp);
        entity.setExpiryTime(LocalDateTime.now().plusMinutes(5));
        otpRepository.save(entity);

        // Send Email
        try {
            notificationService.sendOtpEmail(email, otp);
        } catch (Exception e) {
            System.err.println("Email failed: " + e.getMessage());
        }

        // ✅ RETURN OTP IN DEV MODE 
        return ResponseEntity.ok(Map.of(
            "message", "OTP sent successfully", 
            "success", true,
            "devOtp", otp // ⚠️ REMOVE IN PROD
        ));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String otp = payload.get("otp");

        Otp savedOtp = otpRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("OTP not found or expired"));

        if (!savedOtp.getOtp().equals(otp)) {
            return ResponseEntity.badRequest().body("Invalid OTP");
        }

        if (savedOtp.getExpiryTime().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("OTP expired");
        }

        otpRepository.delete(savedOtp); // Consume OTP

        List<Customer> customers = customerRepository.findByEmail(email);
        
        // ✅ HANDLE NEW USER
        if (customers.isEmpty()) {
            return ResponseEntity.ok(Map.of("isNewUser", true, "email", email));
        }
        
        return ResponseEntity.ok(formatResponse(customers));
    }
    
    // ================= NEW: REGISTER CUSTOMER TO SHOP =================
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String name = payload.get("name");
        String phone = payload.get("phone");
        Long retailerId = Long.parseLong(payload.get("retailerId"));
        
        Retailer retailer = retailerRepository.findById(retailerId)
                .orElseThrow(() -> new RuntimeException("Retailer not found"));
                
        // Check if already exists for this retailer
        // (Simplified: assuming list check or unique constraint, but for now just create)
        
        Customer newCustomer = new Customer();
        newCustomer.setEmail(email);
        newCustomer.setName(name);
        newCustomer.setPhone(phone);
        newCustomer.setRetailer(retailer);
        newCustomer.setDueAmount(0.0);
        newCustomer.setTotalReceived(0.0);
        
        customerRepository.save(newCustomer);
        
        return ResponseEntity.ok(formatResponse(List.of(newCustomer)));
    }
    
    // ================= NEW: LIST ALL RETAILERS (Public) =================
    @GetMapping("/public/retailers")
    public List<Map<String, Object>> getAllRetailers() {
        return retailerRepository.findAll().stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("retailerId", r.getId());
            map.put("name", r.getName() != null ? r.getName() : "Retailer " + r.getId());
            map.put("phone", r.getPhone());
            map.put("isVerified", r.getIsVerified());
            return map;
        }).toList();
    }

    // Helper to format response list
    private List<Map<String, Object>> formatResponse(List<Customer> customers) {
        List<Map<String, Object>> response = new ArrayList<>();
        for (Customer c : customers) {
            Map<String, Object> data = new HashMap<>();
            data.put("customerId", c.getId());
            data.put("retailerId", c.getRetailer().getId());
            data.put("retailerName", c.getRetailer().getName() != null ? c.getRetailer().getName() : "Retailer " + c.getRetailer().getId());
            data.put("retailerPhone", c.getRetailer().getPhone());
            data.put("retailerIsVerified", c.getRetailer().getIsVerified());
            // Add email/name context if needed, but client usually knows
            // Add email/name/phone context
            data.put("customerName", c.getName());
            data.put("customerPhone", c.getPhone());
            data.put("email", c.getEmail());
            
            // ✅ SCHEME DETAILS
            data.put("isSchemeActive", c.getIsSchemeActive());
            data.put("schemeStartDate", c.getSchemeStartDate());
            data.put("schemeMonthlyAmount", c.getSchemeMonthlyAmount());
            data.put("schemeCollectedAmount", c.getSchemeCollectedAmount());
            data.put("schemeTargetAmount", c.getSchemeTargetAmount());
            data.put("schemeMonthsPaid", c.getSchemeMonthsPaid());

            response.add(data);
        }
        return response;
    }
}
