package com.khathabook.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import com.khathabook.model.Product;
import com.khathabook.model.ProductCategory;
import com.khathabook.model.Retailer;
import com.khathabook.repository.ProductReportRepository;
import com.khathabook.repository.ProductRepository;
import com.khathabook.repository.RetailerRepository;
import com.khathabook.service.ProductService;
import com.khathabook.service.StockService;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final StockService stockService;
    private final ProductRepository productRepository;
    private final RetailerRepository retailerRepository;
    private final ProductReportRepository productReportRepository;

    public ProductController(
            ProductService productService,
            StockService stockService,
            ProductRepository productRepository,
            RetailerRepository retailerRepository,
            ProductReportRepository productReportRepository
    ) {
        this.productService = productService;
        this.stockService = stockService;
        this.productRepository = productRepository;
        this.retailerRepository = retailerRepository;
        this.productReportRepository = productReportRepository;
    }

    // =================================================
    // ✅ UPC LOOKUP PROXY (MOVED TO TOP TO AVOID PATH CONFLICTS)
    // =================================================
    @GetMapping("/upc/lookup")
    public ResponseEntity<?> lookupUPC(@RequestParam String barcode) {
        return ResponseEntity.ok(productService.lookupUPC(barcode));
    }

    @GetMapping("/ping")
    public String ping() {
        return "PONG - Version: " + new java.util.Date();
    }

    @GetMapping("/migrate-db")
    public String migrateDb() {
        try {
            System.out.println("🔧 [MIGRATION] Manually triggering index removal...");
            productRepository.dropLegacyIndex();
            return "✅ SUCCESS: Legacy index UK_qfr8vf85k3q1xinifvsl1eynf has been dropped.";
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("check that it exists")) {
                return "ℹ️ INFO: Legacy index not found (likely already dropped).";
            }
            return "❌ ERROR: Failed to drop index: " + msg;
        }
    }

    // =================================================
    // ✅ GET PRODUCTS (OPTIONAL CATEGORY FILTER)
    // =================================================
    @GetMapping
    public List<Product> getProducts(
            @RequestParam Long retailerId,
            @RequestParam(required = false) ProductCategory category,
            @RequestParam(required = false, defaultValue = "false") boolean includeAllStatuses,
            HttpServletRequest request
    ) {
        List<Product> products;
        if (category != null) {
            products = productRepository.findByRetailer_IdAndCategory(retailerId, category);
        } else {
            products = productRepository.findByRetailer_Id(retailerId);
        }
        return products.stream()
                .map(p -> sanitizeProduct(p, request))
                .filter(p -> includeAllStatuses || "APPROVED".equalsIgnoreCase(p.getApprovalStatus()))
                .filter(p -> productReportRepository.countByProductIdAndStatus(p.getId(), "PENDING") < 3)
                .toList();
    }
    
    // =================================================
    // ✅ GET ALL PRODUCTS (GLOBAL)
    // =================================================
    // =================================================
    // ✅ GET ALL PRODUCTS (GLOBAL / NEARBY)
    // =================================================
    @GetMapping("/public/all")
    public List<Product> getAllProducts(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            HttpServletRequest request
    ) {
        List<Product> products;
        if (lat != null && lng != null) {
            List<Retailer> nearestRetailers = retailerRepository.findNearestRetailers(lat, lng, 50, 3);
            if (nearestRetailers.isEmpty()) return List.of();
            products = productRepository.findByRetailerIn(nearestRetailers);
        } else {
            products = productRepository.findAll();
        }
        
        return products.stream()
                .map(p -> sanitizeProduct(p, request))
                .filter(p -> productReportRepository.countByProductIdAndStatus(p.getId(), "PENDING") < 3)
                .toList();
    }

    private Product sanitizeProduct(Product p, HttpServletRequest request) {
        if (p.getImageUrl() != null && p.getImageUrl().contains("localhost:")) {
            String currentBase = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
            // Replace any localhost:#### with the current working host:port
            String sanitized = p.getImageUrl().replaceAll("localhost:\\d+", request.getServerName() + ":" + request.getServerPort());
            p.setImageUrl(sanitized);
        }
        return p;
    }

    // Deprecated filter due to stream mapping above
    private List<Product> filterReportedProducts(List<Product> products) {
        return products.stream()
                .filter(p -> productReportRepository.countByProductIdAndStatus(p.getId(), "PENDING") < 3)
                .toList();
    }

    // =================================================
    // ✅ CREATE PRODUCT
    // =================================================
    @PostMapping
    public ResponseEntity<?> createProduct(
            @RequestBody Product product,
            @RequestParam int boxes,
            @RequestParam Long retailerId
    ) {
        System.out.println("DEBUG: [ProductController] createProduct entry");
        System.out.println("DEBUG: [ProductController] Body: name=" + product.getName() + ", barcode=" + product.getBarcode() + ", type=" + product.getProductType());
        System.out.println("DEBUG: [ProductController] Params: retailerId=" + retailerId + ", boxes=" + boxes);
        try {
            Product saved = productService.createProduct(product, boxes, retailerId);
            productRepository.flush(); 
            
            return ResponseEntity.ok(java.util.Map.of(
                "id", saved.getId(),
                "name", saved.getName(),
                "status", "SUCCESS"
            ));
        } catch (Exception e) {
            System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            System.err.println("❌ ERROR IN createProduct: " + e.getMessage());
            e.printStackTrace();
            System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            
            String logPath = "c:/Users/ASUS/OneDrive/Desktop/wallet/khatha-backend/uploads/debug_log_utf8.txt";
            try (java.io.OutputStreamWriter writer = new java.io.OutputStreamWriter(new java.io.FileOutputStream(logPath, true), java.nio.charset.StandardCharsets.UTF_8);
                 java.io.PrintWriter pw = new java.io.PrintWriter(writer)) {
                pw.println("--- ERROR: " + new java.util.Date() + " ---");
                pw.println("Message: " + e.getMessage());
                e.printStackTrace(pw);
                pw.println("------------------------------------------");
            } catch (java.io.IOException io) {
                io.printStackTrace();
            }
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    // =================================================
    // ✅ ADD STOCK
    // =================================================
    @PostMapping("/{id}/add-stock")
    public ResponseEntity<?> addStock(
            @PathVariable Long id,
            @RequestParam int boxes
    ) {
        try {
            return ResponseEntity.ok(stockService.addStockByBoxes(id, boxes));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // =================================================
    // ✅ DELETE PRODUCT
    // =================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(
            @PathVariable Long id,
            @RequestParam Long retailerId
    ) {
        try {
            productService.deleteProduct(id, retailerId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Cannot delete product: " + e.getMessage());
        }
    }
    
    
    @PutMapping("/{productId}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long productId,
            @RequestBody Product updated
    ) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // ✅ ALLOW SAFE UPDATES
        product.setPrice(updated.getPrice());
        product.setCategory(updated.getCategory());
        product.setUnit(updated.getUnit());

        // 🔐 CONFIG FIELDS (DO NOT BREAK STOCK LOGIC)
        if ("WEIGHT".equalsIgnoreCase(product.getProductType())) {
            product.setBagSizeKg(updated.getBagSizeKg());
        }

        if ("LIQUID".equalsIgnoreCase(product.getProductType())) {
            product.setPacketsPerBox(updated.getPacketsPerBox());
            product.setPacketSize(updated.getPacketSize());
        }	

        if ("UNIT".equalsIgnoreCase(product.getProductType())) {
            product.setUnitsPerBox(updated.getUnitsPerBox());
        }

        // Reset status to PENDING if currently REJECTED
        if ("REJECTED".equalsIgnoreCase(product.getApprovalStatus())) {
            product.setApprovalStatus("PENDING");
        }

        return ResponseEntity.ok(productRepository.save(product));
    }

    // =================================================
    // 📷 PRODUCT IMAGE UPLOAD & RETRIEVAL
    // =================================================
    @PostMapping("/upload-image")
    public ResponseEntity<?> uploadProductImage(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            HttpServletRequest request
    ) {
        try {
            String filename = productService.uploadProductImage(file);
            String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
            String imageUrl = baseUrl + "/api/products/image/" + filename;
            return ResponseEntity.ok(java.util.Map.of("imageUrl", imageUrl, "filename", filename));
        } catch (java.io.IOException e) {
            return ResponseEntity.internalServerError().body("Upload failed: " + e.getMessage());
        }
    }

    @GetMapping(value = "/image/{filename}", produces = org.springframework.http.MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> getProductImage(@PathVariable String filename) {
        try {
            return ResponseEntity.ok(productService.getProductImage(filename));
        } catch (java.io.IOException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
