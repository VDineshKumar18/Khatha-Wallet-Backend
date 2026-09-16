package com.khathabook.controller;

import com.khathabook.model.RetailerRating;
import com.khathabook.repository.RetailerRatingRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/ratings")
public class RetailerRatingController {

    private final RetailerRatingRepository ratingRepository;

    public RetailerRatingController(RetailerRatingRepository ratingRepository) {
        this.ratingRepository = ratingRepository;
    }

    @PostMapping
    public ResponseEntity<?> submitRating(@RequestBody RetailerRating rating) {
        if (rating.getRetailerId() == null || rating.getCustomerId() == null || rating.getOrderId() == null) {
            return ResponseEntity.badRequest().body("RetailerId, CustomerId, and OrderId are required.");
        }

        // Check if already rated
        Optional<RetailerRating> existing = ratingRepository.findByOrderId(rating.getOrderId());
        if (existing.isPresent()) {
            RetailerRating r = existing.get();
            r.setStoreRating(rating.getStoreRating());
            r.setServiceRating(rating.getServiceRating());
            r.setComment(rating.getComment());
            RetailerRating saved = ratingRepository.save(r);
            return ResponseEntity.ok(saved);
        }

        RetailerRating saved = ratingRepository.save(rating);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getRatingByOrder(@PathVariable Long orderId) {
        Optional<RetailerRating> rating = ratingRepository.findByOrderId(orderId);
        if (rating.isPresent()) {
            return ResponseEntity.ok(rating.get());
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/retailer/{retailerId}/average")
    public ResponseEntity<?> getRetailerAverage(@PathVariable Long retailerId) {
        Double avgStore = ratingRepository.getAverageStoreRating(retailerId);
        Long count = ratingRepository.countRatingsByRetailerId(retailerId);
        
        return ResponseEntity.ok(Map.of(
            "retailerId", retailerId,
            "averageRating", avgStore != null ? avgStore : 5.0, // default to 5.0
            "totalRatings", count
        ));
    }
}
