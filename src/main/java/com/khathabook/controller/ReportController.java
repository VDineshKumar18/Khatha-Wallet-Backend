package com.khathabook.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.khathabook.model.Product;
import com.khathabook.model.ProductReport;
import com.khathabook.repository.ProductReportRepository;
import com.khathabook.repository.ProductRepository;

import com.khathabook.service.ReportService;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin
public class ReportController {

    private final ReportService reportService;

    @Autowired
    private ProductReportRepository productReportRepository;

    @Autowired
    private ProductRepository productRepository;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    // =============================
    // ✅ DAILY SALES REPORT
    // =============================
    @GetMapping("/daily")
    public Map<String, Object> dailyReport(
            @RequestHeader("X-Retailer-Id") Long retailerId,
            @RequestParam(required = false) String date) {

        LocalDate reportDate =
                (date == null) ? LocalDate.now() : LocalDate.parse(date);

        return reportService.dailyReport(retailerId, reportDate);
    }

    // =============================
    // ✅ MONTHLY SALES REPORT
    // =============================
    @GetMapping("/monthly")
    public Map<String, Object> monthlyReport(
            @RequestHeader("X-Retailer-Id") Long retailerId,
            @RequestParam int month,
            @RequestParam int year) {

        return reportService.monthlyReport(retailerId, month, year);
    }

    // =============================
    // ✅ PRODUCT REPORTS (Customer Protection)
    // =============================
    @PostMapping("/product")
    public ResponseEntity<?> submitReport(@RequestBody Map<String, String> payload) {
        try {
            Long productId = Long.parseLong(payload.get("productId"));
            String customerEmail = payload.get("customerEmail");
            String reason = payload.get("reason");

            if (customerEmail == null || reason == null || customerEmail.isEmpty() || reason.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Customer email and reason are required", "success", false));
            }

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            ProductReport report = new ProductReport();
            report.setProduct(product);
            report.setCustomerEmail(customerEmail);
            report.setReason(reason);
            report.setCreatedAt(LocalDateTime.now());
            report.setStatus("PENDING");

            productReportRepository.save(report);

            return ResponseEntity.ok(Map.of("message", "Report submitted successfully", "success", true));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("message", "Failed to submit report: " + e.getMessage(), "success", false));
        }
    }
}
