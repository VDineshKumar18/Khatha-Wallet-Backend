package com.khathabook.controller;

import com.khathabook.dto.RetailerDTO;
import com.khathabook.model.Retailer;
import com.khathabook.service.RetailerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@RestController
@RequestMapping("/api/retailer")
// @CrossOrigin(origins = "*")  <-- REMOVED to avoid conflict with SecurityConfig
public class RetailerController {

    private final RetailerService retailerService;
    private final com.khathabook.service.LocationService locationService;
    private final com.khathabook.repository.RetailerRepository retailerRepository;
    private final com.khathabook.repository.RetailerRatingRepository retailerRatingRepository;

    public RetailerController(RetailerService retailerService, 
                             com.khathabook.service.LocationService locationService,
                             com.khathabook.repository.RetailerRepository retailerRepository,
                             com.khathabook.repository.RetailerRatingRepository retailerRatingRepository) {
        this.retailerService = retailerService;
        this.locationService = locationService;
        this.retailerRepository = retailerRepository;
        this.retailerRatingRepository = retailerRatingRepository;
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestHeader("X-Retailer-Id") Long retailerId) {
        System.out.println("Fetching profile for Retailer ID: " + retailerId);
        try {
            Retailer retailer = retailerService.getProfile(retailerId);
            RetailerDTO dto = new RetailerDTO(
                retailer.getId(),
                retailer.getName(),
                retailer.getEmail(),
                retailer.getPhone(),
                retailer.getShopName(),
                retailer.getUpiId(),
                retailer.getPayeeName(),
                retailer.getLatitude(),
                retailer.getLongitude(),
                retailer.getDeliveryRadiusKm(),
                retailer.getSchemeTargetAmount(),
                retailer.getSchemeMonthlyAmount(),
                retailer.getIsVerified(),
                retailer.getApprovalStatus(),
                retailer.getShopLicenseUrl(),
                retailer.getGstNumber(),
                retailer.getShopPhotoUrl()
            );
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("Retailer not found")) {
                return ResponseEntity.status(404).body("Retailer not found. Please re-login.");
            }
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error: " + msg);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @RequestHeader("X-Retailer-Id") Long retailerId,
            @RequestBody RetailerDTO updatedData
    ) {
        System.out.println("Updating profile for Retailer ID: " + retailerId);
        try {
            // Map DTO to Entity (partial update)
            Retailer existing = retailerService.getProfile(retailerId);
            if (updatedData.getName() != null) existing.setName(updatedData.getName());
            if (updatedData.getPhone() != null) existing.setPhone(updatedData.getPhone());
            if (updatedData.getShopName() != null) existing.setShopName(updatedData.getShopName());
            if (updatedData.getUpiId() != null) existing.setUpiId(updatedData.getUpiId());
            if (updatedData.getPayeeName() != null) existing.setPayeeName(updatedData.getPayeeName());
            if (updatedData.getLatitude() != null) existing.setLatitude(updatedData.getLatitude());
            if (updatedData.getLongitude() != null) existing.setLongitude(updatedData.getLongitude());
            if (updatedData.getDeliveryRadiusKm() != null) existing.setDeliveryRadiusKm(updatedData.getDeliveryRadiusKm());
            if (updatedData.getSchemeTargetAmount() != null) existing.setSchemeTargetAmount(updatedData.getSchemeTargetAmount());
            if (updatedData.getSchemeMonthlyAmount() != null) existing.setSchemeMonthlyAmount(updatedData.getSchemeMonthlyAmount());
            
            // Allow service to save (we might need to update service signature or just call repository save directly here, 
            // but better to use service. Let's adapt the service call).
            // Actually, RetailerService expecting Retailer object. Let's create a temporary one or update service?
            // Let's passed the modified 'existing' to service.updateProfile logic.
            
            Retailer saved = retailerService.updateProfile(retailerId, existing);
            
            RetailerDTO dto = new RetailerDTO(
                saved.getId(),
                saved.getName(),
                saved.getEmail(),
                saved.getPhone(),
                saved.getShopName(),
                saved.getUpiId(),
                saved.getPayeeName(),
                saved.getLatitude(),
                saved.getLongitude(),
                saved.getDeliveryRadiusKm(),
                saved.getSchemeTargetAmount(),
                saved.getSchemeMonthlyAmount(),
                saved.getIsVerified(),
                saved.getApprovalStatus(),
                saved.getShopLicenseUrl(),
                saved.getGstNumber(),
                saved.getShopPhotoUrl()
            );
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("Retailer not found")) {
                return ResponseEntity.status(404).body("Retailer not found. Please re-login.");
            }
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error: " + msg);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @PostMapping(value = "/kyc-upload", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadKYC(
            @RequestHeader("X-Retailer-Id") Long retailerId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String type // LICENSE, PHOTO
    ) {
        try {
            String fileName = retailerService.uploadKYCDocument(retailerId, file, type);
            return ResponseEntity.ok(java.util.Collections.singletonMap("fileName", fileName));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Upload failed: " + e.getMessage());
        }
    }
    
    /**
     * Get retailers within delivery radius of customer location
     * GET /api/retailer/nearby?lat=17.385&lng=78.486&radius=10
     */
    @GetMapping("/nearby")
    public ResponseEntity<?> getNearbyRetailers(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(defaultValue = "10") Integer radius) {
        
        try {
            java.util.List<Retailer> allRetailers = retailerRepository.findAll();
            java.util.List<java.util.Map<String, Object>> nearbyRetailers = new java.util.ArrayList<>();
            
            for (Retailer retailer : allRetailers) {
                // Skip retailers without GPS location
                if (retailer.getLatitude() == null || retailer.getLongitude() == null) {
                    continue;
                }
                
                double distance = locationService.calculateDistance(
                    lat, lng, 
                    retailer.getLatitude(), 
                    retailer.getLongitude()
                );
                
                // Check if within delivery radius
                int deliveryRadius = retailer.getDeliveryRadiusKm() != null 
                    ? retailer.getDeliveryRadiusKm() 
                    : radius;
                
                if (distance <= deliveryRadius) {
                    Double avgStore = retailerRatingRepository.getAverageStoreRating(retailer.getId());
                    Long count = retailerRatingRepository.countRatingsByRetailerId(retailer.getId());

                    java.util.Map<String, Object> retailerWithDistance = new java.util.HashMap<>();
                    retailerWithDistance.put("retailerId", retailer.getId());
                    retailerWithDistance.put("name", retailer.getName());
                    retailerWithDistance.put("retailerName", retailer.getName());
                    retailerWithDistance.put("shopName", retailer.getShopName());
                    retailerWithDistance.put("phone", retailer.getPhone());
                    retailerWithDistance.put("retailerPhone", retailer.getPhone());
                    retailerWithDistance.put("email", retailer.getEmail());
                    retailerWithDistance.put("distance", distance);
                    retailerWithDistance.put("schemeTargetAmount", retailer.getSchemeTargetAmount());
                    retailerWithDistance.put("schemeMonthlyAmount", retailer.getSchemeMonthlyAmount());
                    retailerWithDistance.put("isVerified", retailer.getIsVerified());
                    retailerWithDistance.put("rating", avgStore != null ? avgStore : 4.5);
                    retailerWithDistance.put("ratingCount", count);
                    nearbyRetailers.add(retailerWithDistance);
                }
            }
            
            // Sort by distance (nearest first)
            nearbyRetailers.sort((a, b) -> 
                Double.compare((Double) a.get("distance"), (Double) b.get("distance"))
            );
            
            return ResponseEntity.ok(nearbyRetailers);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error finding nearby retailers: " + e.getMessage());
        }
    }
}
