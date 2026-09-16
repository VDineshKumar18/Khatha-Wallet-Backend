package com.khathabook.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_reports")
public class ProductReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "customer_email", nullable = false)
    private String customerEmail;

    @Column(nullable = false)
    private String reason;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    private String status = "PENDING"; // PENDING, RESOLVED, DISMISSED

    public ProductReport() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
