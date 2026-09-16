package com.khathabook.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "products")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ================= BASIC =================
    @Column(nullable = false)
    private String name;

    // ✅ REQUIRED FOR BILLING (Removed global unique=true as it clashes in multi-retailer setup)
    @Column(nullable = false)
    private String barcode;

    @Column(nullable = false)
    private double price;

    // WEIGHT | LIQUID | UNIT
    @Column(nullable = false)
    private String productType;

    @Column(length = 2048)
    private String imageUrl;

    // ✅ CATEGORY
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductCategory category;

    // e.g. 500g, 1kg, 250ml
    private String unit;

    // kg / litre / pcs
    @Column(nullable = false)
    private double quantity = 0;

    // ===== NEW ADMIN MODERATION =====
    @Column(name = "approval_status")
    private String approvalStatus = "PENDING"; // PENDING, APPROVED, REJECTED

    // ================= BUY CONFIG =================
    private Integer bagSizeKg;        // WEIGHT
    private Integer packetsPerBox;    // LIQUID
    private Double packetSize;        // LIQUID
    private Integer unitsPerBox;      // UNIT
    // ==============================================

    private int lowStockAlert = 10;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "retailer_id", nullable = false)
    @JsonIgnore
    private Retailer retailer;

    // ===== GETTERS =====
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getBarcode() { return barcode; }
    public double getPrice() { return price; }
    public String getProductType() { return productType; }
    public String getImageUrl() { return imageUrl; }
    public ProductCategory getCategory() { return category; }
    public double getQuantity() { return quantity; }
    public String getUnit() { return unit; }

    @com.fasterxml.jackson.annotation.JsonProperty("retailerId")
    @jakarta.persistence.Transient
    public Long getRetailerId() {
        return retailer != null ? retailer.getId() : null;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("retailerName")
    @jakarta.persistence.Transient
    public String getRetailerName() {
        return retailer != null ? retailer.getShopName() : null;
    }

    public Integer getBagSizeKg() { return bagSizeKg; }
    public Integer getPacketsPerBox() { return packetsPerBox; }
    public Double getPacketSize() { return packetSize; }
    public Integer getUnitsPerBox() { return unitsPerBox; }

    public int getLowStockAlert() { return lowStockAlert; }
    public Retailer getRetailer() { return retailer; }

    // ===== SETTERS =====
    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    public void setPrice(double price) { this.price = price; }
    public void setProductType(String productType) { this.productType = productType; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setCategory(ProductCategory category) { this.category = category; }
    public void setQuantity(double quantity) { this.quantity = quantity; }
    public void setUnit(String unit) { this.unit = unit; }

    public void setBagSizeKg(Integer bagSizeKg) { this.bagSizeKg = bagSizeKg; }
    public void setPacketsPerBox(Integer packetsPerBox) { this.packetsPerBox = packetsPerBox; }
    public void setPacketSize(Double packetSize) { this.packetSize = packetSize; }
    public void setUnitsPerBox(Integer unitsPerBox) { this.unitsPerBox = unitsPerBox; }

    public void setLowStockAlert(int lowStockAlert) { this.lowStockAlert = lowStockAlert; }
    public void setRetailer(Retailer retailer) { this.retailer = retailer; }

    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }
}
