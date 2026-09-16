package com.khathabook.controller;

import com.khathabook.model.Bill;
import com.khathabook.model.Customer;
import com.khathabook.dto.BillDTO;
import com.khathabook.dto.CustomerDTO;
import com.khathabook.service.BillService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import java.io.IOException;

@RestController
@RequestMapping("/api/bills")
public class BillController {

    private final BillService billService;

    public BillController(BillService billService) {
        this.billService = billService;
    }

    // ================= SAVE KHATHA BILL =================
    @PostMapping(
            value = "/{customerId}",
            consumes = "application/json",
            produces = "application/json"
    )
    public ResponseEntity<?> createBill(
            @PathVariable Long customerId,
            @RequestHeader("X-Retailer-Id") Long retailerId,
            @RequestBody Bill bill
    ) {
        try {
            Bill saved = billService.createBill(customerId, retailerId, bill);
            return ResponseEntity.ok(convertToDTO(saved));
        } catch (RuntimeException e) {
            System.out.println("Error creating bill (Bad Request): " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error creating bill: " + e.getMessage());
        }
    }

    // ================= SAVE PAID BILL =================
    @PostMapping(
            value = "/paid",
            consumes = "application/json",
            produces = "application/json"
    )
    public ResponseEntity<?> createPaidBill(
            @RequestHeader("X-Retailer-Id") Long retailerId,
            @RequestBody Bill bill
    ) {
        try {
            Bill saved = billService.createPaidBill(retailerId, bill);
            return ResponseEntity.ok(convertToDTO(saved));
        } catch (RuntimeException e) {
            System.out.println("Error creating paid bill (Bad Request): " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error creating paid bill: " + e.getMessage());
        }
    }

    // ================= GET PREVIOUS BILLS =================
    @GetMapping("/{customerId}")
    public ResponseEntity<List<BillDTO>> getBillsByCustomer(
            @PathVariable Long customerId,
            @RequestHeader("X-Retailer-Id") Long retailerId
    ) {
        List<Bill> bills = billService.getBillsByCustomer(customerId, retailerId);
        List<BillDTO> dtos = bills.stream().map(this::convertToDTO).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // ================= GET ALL BILLS =================
    @GetMapping
    public ResponseEntity<List<BillDTO>> getAllBills(
            @RequestHeader("X-Retailer-Id") Long retailerId
    ) {
        List<Bill> bills = billService.getAllBills(retailerId);
        List<BillDTO> dtos = bills.stream().map(this::convertToDTO).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // ================= UPLOAD IMAGE =================
    @PostMapping(value = "/{billId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadBillImage(
            @PathVariable Long billId,
            @RequestHeader("X-Retailer-Id") Long retailerId,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            String filename = billService.uploadBillImage(billId, retailerId, file);
            return ResponseEntity.ok(filename);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Upload failed: " + e.getMessage());
        }
    }

    // ================= GET IMAGE =================
    @GetMapping(value = "/image/{filename}", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> getBillImage(@PathVariable String filename) {
        try {
            return ResponseEntity.ok(billService.getBillImage(filename));
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ================= UPDATE BILL =================
    @PutMapping("/{id}")
    public ResponseEntity<?> updateBill(
            @PathVariable Long id,
            @RequestHeader("X-Retailer-Id") Long retailerId,
            @RequestBody Bill bill
    ) {
        try {
            Bill updated = billService.updateBill(id, bill, retailerId);
            return ResponseEntity.ok(convertToDTO(updated));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    // ================= RECYCLE BIN OPs =================

    @GetMapping("/recycled")
    public ResponseEntity<List<BillDTO>> getRecycledBills(
            @RequestHeader("X-Retailer-Id") Long retailerId
    ) {
        List<Bill> bills = billService.getRecycledBills(retailerId);
        List<BillDTO> dtos = bills.stream().map(this::convertToDTO).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> softDeleteBill(
            @PathVariable Long id,
            @RequestHeader("X-Retailer-Id") Long retailerId
    ) {
        try {
            billService.deleteBill(id, retailerId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/restore")
    public ResponseEntity<?> restoreBill(
            @PathVariable Long id,
            @RequestHeader("X-Retailer-Id") Long retailerId
    ) {
        try {
            billService.restoreBill(id, retailerId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<?> permanentDeleteBill(
            @PathVariable Long id,
            @RequestHeader("X-Retailer-Id") Long retailerId
    ) {
        try {
            billService.permanentDeleteBill(id, retailerId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private BillDTO convertToDTO(Bill bill) {
        BillDTO dto = new BillDTO();
        dto.setId(bill.getId());
        dto.setBillNumber(bill.getBillNumber());
        dto.setBillDate(bill.getBillDate());
        dto.setAmount(bill.getAmount());
        dto.setStatus(bill.getStatus());
        dto.setType(bill.getType());
        dto.setItems(bill.getItems());
        dto.setImageUrl(bill.getImageUrl());
        dto.setPaid(bill.isPaid());
        dto.setPaymentMode(bill.getPaymentMode());
        dto.setPaidAmount(bill.getPaidAmount());
        dto.setDueAmount(bill.getDueAmount());
        dto.setGatewayTransactionRef(bill.getGatewayTransactionRef());

        if (bill.getCustomer() != null) {
            Customer c = bill.getCustomer();
            dto.setCustomer(new CustomerDTO(c.getId(), c.getName(), c.getPhone(), c.getEmail()));
        }
        return dto;
    }
}
