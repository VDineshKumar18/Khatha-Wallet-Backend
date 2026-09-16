package com.khathabook.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "bills")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;
    private double amount;
    @Column(columnDefinition = "TEXT")
    private String items;
    private String imageUrl; // ✅ New Field for Bill Image

    @Column(nullable = false)
    private boolean refunded = false;

    @Column(nullable = false)
    private boolean isDeleted = false; // ✅ Soft delete flag for Recycle Bin

    private Double refundAmount;
    private java.time.LocalDate refundDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd['T'HH:mm[:ss][.SSS]['Z']]")
    private LocalDateTime billDate;

    // ===== PAYMENT =====
    private String paymentMode; // CASH, UPI, CARD, BANK
    private boolean paid;

    // ===== BILL META =====
    @Column(unique = true)
    private String billNumber;

    private double paidAmount;
    private double dueAmount;

    private String status; // PAID / PARTIAL / DUE

    // ================= NEW (SAFE ADDITION) =================
    private int loyaltyPointsUsed;      // points redeemed
    private double discountAmount;      // ₹ value
    private String gatewayTransactionRef;
    // ======================================================

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = true)
    @JsonIgnore
    private Customer customer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "retailer_id", nullable = false)
    @JsonIgnore
    private Retailer retailer;

    // ===== GETTERS & SETTERS =====

    public Long getId() { return id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getItems() { return items; }
    public void setItems(String items) { this.items = items; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public LocalDateTime getBillDate() { return billDate; }
    public void setBillDate(LocalDateTime billDate) { this.billDate = billDate; }

    public String getPaymentMode() { return paymentMode; }
    public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }

    public boolean isPaid() { return paid; }
    public void setPaid(boolean paid) { this.paid = paid; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public Retailer getRetailer() { return retailer; }
    public void setRetailer(Retailer retailer) { this.retailer = retailer; }

    public String getBillNumber() { return billNumber; }
    public void setBillNumber(String billNumber) { this.billNumber = billNumber; }

    public double getPaidAmount() { return paidAmount; }
    public void setPaidAmount(double paidAmount) { this.paidAmount = paidAmount; }

    public double getDueAmount() { return dueAmount; }
    public void setDueAmount(double dueAmount) { this.dueAmount = dueAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isRefunded() { return refunded; }
    public void setRefunded(boolean refunded) { this.refunded = refunded; }

    public Double getRefundAmount() { return refundAmount; }
    public void setRefundAmount(Double refundAmount) { this.refundAmount = refundAmount; }

    public LocalDate getRefundDate() { return refundDate; }
    public void setRefundDate(LocalDate refundDate) { this.refundDate = refundDate; }

    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean isDeleted) { this.isDeleted = isDeleted; }

    // ===== NEW GETTERS =====
    public int getLoyaltyPointsUsed() { return loyaltyPointsUsed; }
    public void setLoyaltyPointsUsed(int loyaltyPointsUsed) {
        this.loyaltyPointsUsed = loyaltyPointsUsed;
    }

    public double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public String getGatewayTransactionRef() { return gatewayTransactionRef; }
    public void setGatewayTransactionRef(String gatewayTransactionRef) {
        this.gatewayTransactionRef = gatewayTransactionRef;
    }
}
