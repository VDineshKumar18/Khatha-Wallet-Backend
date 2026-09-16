package com.khathabook.controller;

import com.khathabook.model.GatewayTransaction;
import com.khathabook.repository.GatewayTransactionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments/gateway")
@CrossOrigin(origins = "*")
public class PaymentGatewayController {

    private final GatewayTransactionRepository gatewayTxnRepository;

    public PaymentGatewayController(GatewayTransactionRepository gatewayTxnRepository) {
        this.gatewayTxnRepository = gatewayTxnRepository;
    }

    @PostMapping("/charge")
    public ResponseEntity<?> processCharge(@RequestBody Map<String, Object> payload) {
        try {
            double amount = Double.parseDouble(payload.get("amount").toString());
            String customerEmail = (String) payload.get("customerEmail");
            String customerName = (String) payload.get("customerName");
            String paymentMethod = (String) payload.get("paymentMethod");
            Long retailerId = Long.parseLong(payload.get("retailerId").toString());

            // Mock Card / UPI details checking
            if ("CARD".equalsIgnoreCase(paymentMethod)) {
                String cardNumber = (String) payload.get("cardNumber");
                if (cardNumber == null || cardNumber.replace(" ", "").length() < 16) {
                    return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Invalid card details."));
                }
            } else if ("UPI".equalsIgnoreCase(paymentMethod)) {
                String upiId = (String) payload.get("upiId");
                if (upiId == null || !upiId.contains("@")) {
                    return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Invalid UPI ID."));
                }
            }

            // Generate gateway transaction ref
            String transactionRef = "PAY-" + UUID.randomUUID().toString().substring(0, 13).toUpperCase();

            // Save to DB
            GatewayTransaction txn = new GatewayTransaction(
                    transactionRef,
                    amount,
                    customerEmail,
                    customerName,
                    paymentMethod,
                    "SUCCESS",
                    retailerId
            );
            gatewayTxnRepository.save(txn);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "transactionRef", transactionRef,
                    "message", "Payment processed successfully"
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Gateway Error: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<GatewayTransaction>> getTransactions(@RequestParam(required = false) Long retailerId) {
        if (retailerId != null) {
            return ResponseEntity.ok(gatewayTxnRepository.findByRetailerId(retailerId));
        }
        return ResponseEntity.ok(gatewayTxnRepository.findAll());
    }
}
