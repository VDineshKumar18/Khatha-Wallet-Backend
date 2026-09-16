package com.khathabook.service;

import com.khathabook.model.Bill;
import com.khathabook.model.Customer;
import com.khathabook.model.Product;
import com.khathabook.repository.BillRepository;
import com.khathabook.repository.CustomerRepository;
import com.khathabook.repository.ProductRepository;
import com.khathabook.repository.RetailerRepository;
import com.khathabook.model.Retailer;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional; // ✅ REQUIRED IMPORT
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.springframework.web.multipart.MultipartFile;

@Service
public class BillService {

    private final BillRepository billRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final RetailerRepository retailerRepository;
    private final NotificationService notificationService;

    public BillService(
            BillRepository billRepository,
            CustomerRepository customerRepository,
            ProductRepository productRepository,
            RetailerRepository retailerRepository,
            NotificationService notificationService
    ) {
        this.billRepository = billRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.retailerRepository = retailerRepository;
        this.notificationService = notificationService;
    }

    // ================= CREATE KHATHA BILL =================
    public Bill createBill(Long customerId, Long retailerId, Bill bill) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        if (!customer.getRetailer().getId().equals(retailerId)) {
            throw new RuntimeException("Unauthorized");
        }

        bill.setCustomer(customer);
        bill.setRetailer(customer.getRetailer());
        if (bill.getBillDate() == null) {
            bill.setBillDate(LocalDateTime.now());
        }

        int pointsUsed = bill.getLoyaltyPointsUsed();
        double discount = 0;

        if ("SALE".equalsIgnoreCase(bill.getType()) && pointsUsed > 0) {
            if (customer.getLoyaltyPoints() < pointsUsed) {
                throw new RuntimeException("Not enough loyalty points");
            }
            discount = pointsUsed;
            customer.setLoyaltyPoints(customer.getLoyaltyPoints() - pointsUsed);
        }

        bill.setDiscountAmount(discount);

        double finalAmount = bill.getAmount() - discount;
        if (finalAmount < 0) finalAmount = 0;

        bill.setAmount(finalAmount);

        bill.setBillNumber(generateBillNumber());
        bill.setPaidAmount(bill.getPaidAmount());
        bill.setDueAmount(finalAmount - bill.getPaidAmount());

        if (bill.getDueAmount() <= 0) {
            bill.setStatus("PAID");
            bill.setPaid(true);
            bill.setDueAmount(0);
        } else if (bill.getPaidAmount() > 0) {
            bill.setStatus("PARTIAL");
        } else {
            bill.setStatus("DUE");
        }

        if ("SALE".equalsIgnoreCase(bill.getType())) {
            reduceStockFromBill(bill.getItems(), retailerId);
        }

        if ("GAVE".equalsIgnoreCase(bill.getType())) {
            // ✅ Only add the balance (due) to customer debt, not the full bill total
            customer.setDueAmount(customer.getDueAmount() + bill.getDueAmount());
        } else if ("RECEIVED".equalsIgnoreCase(bill.getType())) {
            customer.setDueAmount(customer.getDueAmount() - finalAmount);
            customer.setTotalReceived(customer.getTotalReceived() + finalAmount); // ✅ Track Total Received
        } else if ("SCHEME".equalsIgnoreCase(bill.getType())) {
            // ✅ SCHEME PAYMENT LOGIC
            customer.setSchemeCollectedAmount(customer.getSchemeCollectedAmount() + finalAmount);
            if (customer.getSchemeMonthlyAmount() > 0 && finalAmount >= customer.getSchemeMonthlyAmount()) {
                 customer.setSchemeMonthsPaid(customer.getSchemeMonthsPaid() + 1);
            }
        }

        // ✅ HANDLE PRODUCT BILLS (SALE or GAVE)
        if ("SALE".equalsIgnoreCase(bill.getType()) || "GAVE".equalsIgnoreCase(bill.getType())) {
            // Track paid amount if any
            if (bill.getPaidAmount() > 0) {
                customer.setTotalReceived(customer.getTotalReceived() + bill.getPaidAmount());
            }
            
            // Earn loyalty points on total purchase value:
            // - 10 points per 100/- for Cash, UPI, Netbanking, Cards (non-Khatha)
            // - 5 points per 100/- for Khatha
            int earned = (int) (finalAmount / 100);
            if ("KHATHA".equalsIgnoreCase(bill.getPaymentMode())) {
                earned *= 5;
            } else {
                earned *= 10;
            }
            customer.setLoyaltyPoints(customer.getLoyaltyPoints() + earned);
        }

        customerRepository.save(customer);

        Bill savedBill = billRepository.save(bill);
        notificationService.sendBillEmail(savedBill, retailerId);

        return savedBill;
    }

    // ================= CREATE PAID BILL =================
    public Bill createPaidBill(Long retailerId, Bill bill) {

        // ✅ Fetch retailer and associate with walk-in bill
        Retailer retailer = retailerRepository.findById(retailerId)
                .orElseThrow(() -> new RuntimeException("Retailer not found"));

        bill.setRetailer(retailer);
        bill.setPaid(true);
        bill.setCustomer(null);
        if (bill.getBillDate() == null) {
            bill.setBillDate(LocalDateTime.now());
        }

        bill.setBillNumber(generateBillNumber());
        bill.setPaidAmount(bill.getAmount());
        bill.setDueAmount(0);
        bill.setStatus("PAID");

        reduceStockFromBill(bill.getItems(), retailerId);

        return billRepository.save(bill);
    }

    // ================= GET CUSTOMER BILLS =================
    public List<Bill> getBillsByCustomer(Long customerId, Long retailerId) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        if (!customer.getRetailer().getId().equals(retailerId)) {
            throw new RuntimeException("Unauthorized");
        }

        return billRepository.findByCustomerIdAndRetailerId(customerId, retailerId);
    }

    // ================= GET ALL BILLS (RETAILER) =================
    public List<Bill> getAllBills(Long retailerId) {
        return billRepository.findByRetailerIdAndIsDeletedFalseOrderByBillDateDesc(retailerId);
    }

    // ================= STOCK REDUCTION =================
    public void reduceStockFromBill(String items, Long retailerId) {

        if (items == null || items.isBlank()) return;

        String[] billItems = items.split(",");

        for (String entry : billItems) {

            String[] parts = entry.trim().split(" x");
            if (parts.length != 2) continue;

            String barcode = parts[0].trim();
            double soldQty = Double.parseDouble(parts[1].trim()); // ✅ Allow Decimals (e.g., 1.5 kg)

            Product product;

            // ✅ STEP 1: Try safe single-result query
            Optional<Product> single =
                    productRepository.findByBarcodeAndRetailer_Id(barcode, retailerId);

            if (single.isPresent()) {
                product = single.get();
            } else {
                // ✅ STEP 2: Fallback for duplicate barcodes (legacy data)
                List<Product> products =
                        productRepository.findAllByBarcodeAndRetailer_Id(barcode, retailerId);

                if (products.isEmpty()) {
                    throw new RuntimeException(
                            "Product not found for barcode: " + barcode
                    );
                }

                // ✅ Pick the latest product safely
                product = products.stream()
                        .max((a, b) -> a.getId().compareTo(b.getId()))
                        .orElseThrow();
            }

            String type = product.getProductType();
            if (type == null) {
                throw new RuntimeException(
                        "Product type missing for " + product.getName()
                );
            }

            type = type.trim().toUpperCase();
            double stockToReduce;

            switch (type) {

                case "WEIGHT" -> {
                    // ✅ FIXED: Deduct exact weight entered (e.g., 1.5 kg)
                    // Old logic incorrectly multiplied by BagSize (treating input as bags)
                    stockToReduce = soldQty;
                }

                case "LIQUID" -> {
                    // ✅ FIXED: Deduct exact volume entered (e.g., 0.5 L)
                    // Warning: If frontend shows 'L', user enters Litres.
                    stockToReduce = soldQty; 
                }

                case "UNIT" -> stockToReduce = soldQty;

                default -> throw new RuntimeException(
                        "Invalid product type '" + type + "' for " + product.getName()
                );
            }

            if (product.getQuantity() < stockToReduce) {
                throw new RuntimeException(
                        "Insufficient stock for " + product.getName()
                );
            }

            product.setQuantity(product.getQuantity() - stockToReduce);
            productRepository.save(product);
        }
    }

    private String generateBillNumber() {
        return "BILL-" + System.currentTimeMillis();
    }

    // ================= IMAGE UPLOAD LOGIC =================
    private static final String UPLOAD_DIR = "uploads/";

    public String uploadBillImage(Long billId, Long retailerId, MultipartFile file) throws IOException {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new RuntimeException("Bill not found"));

        if (bill.getCustomer() != null && !bill.getCustomer().getRetailer().getId().equals(retailerId)) {
             throw new RuntimeException("Unauthorized");
        } else if (bill.getRetailer() != null && !bill.getRetailer().getId().equals(retailerId)) {
             throw new RuntimeException("Unauthorized");
        }

        // Create directory if not exists
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String filename = "bill_" + billId + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(filename);

        // Save file
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Update Bill
        bill.setImageUrl(filename);
        billRepository.save(bill);

        return filename;
    }

    public byte[] getBillImage(String filename) throws IOException {
        Path filePath = Paths.get(UPLOAD_DIR).resolve(filename);
        if (!Files.exists(filePath)) {
            throw new RuntimeException("File not found");
        }
        return Files.readAllBytes(filePath);
    }

    // ================= UPDATE BILL =================
    public Bill updateBill(Long id, Bill billDetails, Long retailerId) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bill not found"));

        if (!bill.getRetailer().getId().equals(retailerId)) {
            throw new RuntimeException("Unauthorized");
        }

        // Only allow updating Payment details for now to avoid Stock corruption
        bill.setStatus(billDetails.getStatus());
        bill.setPaymentMode(billDetails.getPaymentMode());
        bill.setPaidAmount(billDetails.getPaidAmount());
        bill.setDueAmount(billDetails.getDueAmount());
        bill.setAmount(billDetails.getAmount()); // Allow correcting total if needed, but be careful
        
        // Auto-update Paid status
        if(bill.getDueAmount() <= 0) {
            bill.setStatus("PAID");
            bill.setPaid(true);
        } else {
            bill.setPaid(false);
        }

        return billRepository.save(bill);
    }

    // ================= RESTORE STOCK =================
    public void restoreStock(String items, Long retailerId) {
        if (items == null || items.isBlank()) return;

        String[] billItems = items.split(",");

        for (String entry : billItems) {
            String[] parts = entry.trim().split(" x");
            if (parts.length != 2) continue;

            String barcode = parts[0].trim();
            double quantity = Double.parseDouble(parts[1].trim());

            Product product = productRepository.findByBarcodeAndRetailer_Id(barcode, retailerId)
                    .orElse(null);

            if (product != null) {
                // Determine how much to add back based on product type
                // NOTE: logic should mirror reduceStockFromBill regarding units
                // Since reduceStockFromBill treats 'quantity' as exact units for WEIGHT/LIQUID/UNIT
                // We simply add it back.
                
                product.setQuantity(product.getQuantity() + quantity);
                productRepository.save(product);
            }
        }
    }


    // ================= RECYCLE BIN OPs =================
    public List<Bill> getRecycledBills(Long retailerId) {
        return billRepository.findByRetailerIdAndIsDeletedTrueOrderByBillDateDesc(retailerId);
    }

    public void deleteBill(Long id, Long retailerId) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bill not found"));

        if (!bill.getRetailer().getId().equals(retailerId)) {
            throw new RuntimeException("Unauthorized");
        }

        // SOFT DELETE
        bill.setDeleted(true);
        billRepository.save(bill);
    }

    public void restoreBill(Long id, Long retailerId) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bill not found"));

        if (!bill.getRetailer().getId().equals(retailerId)) {
            throw new RuntimeException("Unauthorized");
        }

        bill.setDeleted(false);
        billRepository.save(bill);
    }

    public void permanentDeleteBill(Long id, Long retailerId) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bill not found"));

        if (!bill.getRetailer().getId().equals(retailerId)) {
            throw new RuntimeException("Unauthorized");
        }

        if ("SALE".equalsIgnoreCase(bill.getType())) {
            restoreStock(bill.getItems(), retailerId);
        }

        billRepository.delete(bill);
    }

}
