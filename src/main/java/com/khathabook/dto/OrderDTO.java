package com.khathabook.dto;

public class OrderDTO {
    private Long id;
    private Long customerId;
    private Long retailerId;
    private String retailerName; 
    private String customerName; // ✅ ADDED
    private String customerPhone; // ✅ ADDED
    private String items; // JSON String
    private double totalAmount;
    private String paymentMode; // CASH, UPI, KHATHA
    private String status;
    private String orderDate;
    private Integer unreadChatCount; // ✅ ADDED
    private String gatewayTransactionRef; // ✅ ADDED

    // State Machine Fields
    private String acceptedAt;
    private String expectedPackingTime;
    private String readyForPickupAt;
    private String packedAt;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public Long getRetailerId() { return retailerId; }
    public void setRetailerId(Long retailerId) { this.retailerId = retailerId; }

    public String getRetailerName() { return retailerName; }
    public void setRetailerName(String retailerName) { this.retailerName = retailerName; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getItems() { return items; }
    public void setItems(String items) { this.items = items; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getPaymentMode() { return paymentMode; }
    public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getOrderDate() { return orderDate; }
    public void setOrderDate(String orderDate) { this.orderDate = orderDate; }

    public Integer getUnreadChatCount() { return unreadChatCount; }
    public void setUnreadChatCount(Integer unreadChatCount) { this.unreadChatCount = unreadChatCount; }

    public String getAcceptedAt() { return acceptedAt; }
    public void setAcceptedAt(String acceptedAt) { this.acceptedAt = acceptedAt; }

    public String getExpectedPackingTime() { return expectedPackingTime; }
    public void setExpectedPackingTime(String expectedPackingTime) { this.expectedPackingTime = expectedPackingTime; }

    public String getReadyForPickupAt() { return readyForPickupAt; }
    public void setReadyForPickupAt(String readyForPickupAt) { this.readyForPickupAt = readyForPickupAt; }

    public String getPackedAt() { return packedAt; }
    public void setPackedAt(String packedAt) { this.packedAt = packedAt; }

    public String getGatewayTransactionRef() { return gatewayTransactionRef; }
    public void setGatewayTransactionRef(String gatewayTransactionRef) { this.gatewayTransactionRef = gatewayTransactionRef; }
}
