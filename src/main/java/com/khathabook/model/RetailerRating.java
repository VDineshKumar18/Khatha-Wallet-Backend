package com.khathabook.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "retailer_ratings", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"order_id"})
})
public class RetailerRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "retailer_id", nullable = false)
    private Long retailerId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "store_rating", nullable = false)
    private double storeRating;

    @Column(name = "service_rating", nullable = false)
    private double serviceRating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    private LocalDateTime createdAt = LocalDateTime.now();

    public RetailerRating() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getRetailerId() { return retailerId; }
    public void setRetailerId(Long retailerId) { this.retailerId = retailerId; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public double getStoreRating() { return storeRating; }
    public void setStoreRating(double storeRating) { this.storeRating = storeRating; }

    public double getServiceRating() { return serviceRating; }
    public void setServiceRating(double serviceRating) { this.serviceRating = serviceRating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
