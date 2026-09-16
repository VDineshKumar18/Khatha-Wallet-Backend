package com.khathabook.service;

import com.khathabook.model.Retailer;
import com.khathabook.repository.RetailerRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.Optional;

@Service
public class RetailerService {

    private final RetailerRepository retailerRepository;

    public RetailerService(RetailerRepository retailerRepository) {
        this.retailerRepository = retailerRepository;
    }

    public Retailer getProfile(Long id) {
        return retailerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Retailer not found"));
    }

    public Retailer updateProfile(Long id, Retailer updatedData) {
        Retailer retailer = getProfile(id);

        if (updatedData.getName() != null) retailer.setName(updatedData.getName());
        if (updatedData.getPhone() != null) retailer.setPhone(updatedData.getPhone());
        if (updatedData.getUpiId() != null) retailer.setUpiId(updatedData.getUpiId());
        if (updatedData.getPayeeName() != null) retailer.setPayeeName(updatedData.getPayeeName());
        
        // Fix: Map all profile fields to ensure they are saved to the database
        if (updatedData.getShopName() != null) retailer.setShopName(updatedData.getShopName());
        if (updatedData.getDeliveryRadiusKm() != null) retailer.setDeliveryRadiusKm(updatedData.getDeliveryRadiusKm());
        if (updatedData.getLatitude() != null) retailer.setLatitude(updatedData.getLatitude());
        if (updatedData.getLongitude() != null) retailer.setLongitude(updatedData.getLongitude());
        if (updatedData.getSchemeTargetAmount() != null) retailer.setSchemeTargetAmount(updatedData.getSchemeTargetAmount());
        if (updatedData.getSchemeMonthlyAmount() != null) retailer.setSchemeMonthlyAmount(updatedData.getSchemeMonthlyAmount());
        
        // innovative: handle email updates if needed, but usually email is identity.
        // Let's keep email immutable for simplicity or handle it carefully with unique checks.
        // For now, only name/phone as requested.

        return retailerRepository.save(retailer);
    }

    private static final String UPLOAD_DIR = "uploads/";

    public String uploadKYCDocument(Long retailerId, MultipartFile file, String docType) throws IOException {
        Retailer retailer = getProfile(retailerId);

        // Create directory if not exists
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String filename = "kyc_" + docType + "_" + retailerId + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(filename);

        // Save file
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Update Retailer
        if ("LICENSE".equalsIgnoreCase(docType)) {
            retailer.setShopLicenseUrl(filename);
        } else if ("PHOTO".equalsIgnoreCase(docType)) {
            retailer.setShopPhotoUrl(filename);
        }
        
        retailerRepository.save(retailer);

        return filename;
    }
}
