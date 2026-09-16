package com.khathabook.dto;

import java.time.LocalDateTime;

public class BillDTO {
    private Long id;
    private String billNumber;
    private LocalDateTime billDate;
    private double amount;
    private String status;
    private CustomerDTO customer;
    
    // Additional fields used by frontend or useful
    private String type;
    private String items;
    private String imageUrl;
    private boolean paid;
    private String paymentMode;
    private double paidAmount;
    private double dueAmount;
    private String gatewayTransactionRef;

    public BillDTO(Long id, String billNumber, LocalDateTime billDate, double amount, String status, CustomerDTO customer) {
        this.id = id;
        this.billNumber = billNumber;
        this.billDate = billDate;
        this.amount = amount;
        this.status = status;
        this.customer = customer;
    }

    // Full constructor
    public BillDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBillNumber() { return billNumber; }
    public void setBillNumber(String billNumber) { this.billNumber = billNumber; }

    public LocalDateTime getBillDate() { return billDate; }
    public void setBillDate(LocalDateTime billDate) { this.billDate = billDate; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public CustomerDTO getCustomer() { return customer; }
    public void setCustomer(CustomerDTO customer) { this.customer = customer; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getItems() { return items; }
    public void setItems(String items) { this.items = items; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public boolean isPaid() { return paid; }
    public void setPaid(boolean paid) { this.paid = paid; }

    public String getPaymentMode() { return paymentMode; }
    public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }

    public double getPaidAmount() { return paidAmount; }
    public void setPaidAmount(double paidAmount) { this.paidAmount = paidAmount; }

    public double getDueAmount() { return dueAmount; }
    public void setDueAmount(double dueAmount) { this.dueAmount = dueAmount; }

    public String getGatewayTransactionRef() { return gatewayTransactionRef; }
    public void setGatewayTransactionRef(String gatewayTransactionRef) { this.gatewayTransactionRef = gatewayTransactionRef; }
}
