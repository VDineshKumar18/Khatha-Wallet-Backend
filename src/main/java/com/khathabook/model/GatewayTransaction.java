package com.khathabook.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "gateway_transactions")
public class GatewayTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String transactionRef;

    private double amount;
    private String customerEmail;
    private String customerName;
    private String paymentMethod; // CARD, UPI, NETBANKING
    private String status; // SUCCESS, FAILED, DISPUTED
    private LocalDateTime createdAt;
    private Long retailerId;

    public GatewayTransaction() {
        this.createdAt = LocalDateTime.now();
    }

    public GatewayTransaction(String transactionRef, double amount, String customerEmail, String customerName, String paymentMethod, String status, Long retailerId) {
        this.transactionRef = transactionRef;
        this.amount = amount;
        this.customerEmail = customerEmail;
        this.customerName = customerName;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.retailerId = retailerId;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTransactionRef() { return transactionRef; }
    public void setTransactionRef(String transactionRef) { this.transactionRef = transactionRef; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Long getRetailerId() { return retailerId; }
    public void setRetailerId(Long retailerId) { this.retailerId = retailerId; }
}
