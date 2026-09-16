package com.khathabook.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "customer_orders") // 'orders' is a reserved keyword in some DBs
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIgnoreProperties({"orders", "bills", "retailer"}) // Prevent recursion
    private Customer customer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "retailer_id", nullable = false)
    @JsonIgnoreProperties({"orders", "bills", "customers"})
    private Retailer retailer;

    @Column(columnDefinition = "TEXT")
    private String items; // JSON string of items: [{"name": "Sugar", "qty": "1kg", "price": 40}, ...]

    private double totalAmount;

    private String paymentMode; // CASH, UPI, KHATHA

    private String gatewayTransactionRef; // Gateway transaction reference ID

    private String status; // PENDING, PACKED, COMPLETED, CANCELLED

    private String deliveryOtp; // ✅ OTP for delivery verification

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime orderDate;

    // State Machine Timestamps
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime acceptedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expectedPackingTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime readyForPickupAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime packedAt;
    
    // Auto-set date
    @PrePersist
    protected void onCreate() {
        if (this.orderDate == null) {
            this.orderDate = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = "PLACED";
        }
    }

    // ===== GETTERS & SETTERS =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public Retailer getRetailer() { return retailer; }
    public void setRetailer(Retailer retailer) { this.retailer = retailer; }

    public String getItems() { return items; }
    public void setItems(String items) { this.items = items; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getPaymentMode() { return paymentMode; }
    public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }

    public String getGatewayTransactionRef() { return gatewayTransactionRef; }
    public void setGatewayTransactionRef(String gatewayTransactionRef) { this.gatewayTransactionRef = gatewayTransactionRef; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }

    public String getDeliveryOtp() { return deliveryOtp; }
    public void setDeliveryOtp(String deliveryOtp) { this.deliveryOtp = deliveryOtp; }

    public LocalDateTime getAcceptedAt() { return acceptedAt; }
    public void setAcceptedAt(LocalDateTime acceptedAt) { this.acceptedAt = acceptedAt; }

    public LocalDateTime getExpectedPackingTime() { return expectedPackingTime; }
    public void setExpectedPackingTime(LocalDateTime expectedPackingTime) { this.expectedPackingTime = expectedPackingTime; }

    public LocalDateTime getReadyForPickupAt() { return readyForPickupAt; }
    public void setReadyForPickupAt(LocalDateTime readyForPickupAt) { this.readyForPickupAt = readyForPickupAt; }

    public LocalDateTime getPackedAt() { return packedAt; }
    public void setPackedAt(LocalDateTime packedAt) { this.packedAt = packedAt; }
}
