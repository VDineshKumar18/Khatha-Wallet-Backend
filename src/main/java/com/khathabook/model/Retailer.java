package com.khathabook.model;

import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "retailers")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Retailer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    private String name;
    private String phone;
    
    @Column(name = "upi_id")
    private String upiId;
    
    @Column(name = "payee_name")
    private String payeeName;
    
    @Column(name = "shop_name")
    private String shopName;
    
    @Column(name = "latitude")
    private Double latitude;
    
    @Column(name = "longitude")
    private Double longitude;
    
    @Column(name = "delivery_radius_km")
    private Integer deliveryRadiusKm = 10; // Default 10km

    @Column(name = "scheme_target_amount")
    private Double schemeTargetAmount = 6000.0; // Default Target

    @Column(name = "scheme_monthly_amount")
    private Double schemeMonthlyAmount = 500.0; // Default Monthly

    @Column(name = "is_verified")
    private Boolean isVerified = false;

    // ===== NEW ADMIN VERIFICATION & KYC FIELDS =====
    @Column(name = "approval_status")
    private String approvalStatus = "PENDING"; // PENDING, APPROVED, SUSPENDED, REJECTED

    @Column(name = "shop_license_url")
    private String shopLicenseUrl;

    @Column(name = "gst_number")
    private String gstNumber;

    @Column(name = "shop_photo_url")
    private String shopPhotoUrl;

    // 🔴 DO NOT serialize relationships
    @OneToMany(mappedBy = "retailer", fetch = FetchType.LAZY)
    @JsonIgnore
    private java.util.List<Customer> customers;

    @OneToMany(mappedBy = "retailer", fetch = FetchType.LAZY)
    @JsonIgnore
    private java.util.List<Bill> bills;

    public Retailer() {}

    public Retailer(String email) {
        this.email = email;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }

    public void setId(Long id) { this.id = id; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getUpiId() { return upiId; }
    public void setUpiId(String upiId) { this.upiId = upiId; }

    public String getPayeeName() { return payeeName; }
    public void setPayeeName(String payeeName) { this.payeeName = payeeName; }
    
    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }
    
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    
    public Integer getDeliveryRadiusKm() { return deliveryRadiusKm; }
    public void setDeliveryRadiusKm(Integer deliveryRadiusKm) { this.deliveryRadiusKm = deliveryRadiusKm; }

    public Double getSchemeTargetAmount() { return schemeTargetAmount; }
    public void setSchemeTargetAmount(Double schemeTargetAmount) { this.schemeTargetAmount = schemeTargetAmount; }

    public Double getSchemeMonthlyAmount() { return schemeMonthlyAmount; }
    public void setSchemeMonthlyAmount(Double schemeMonthlyAmount) { this.schemeMonthlyAmount = schemeMonthlyAmount; }

    public Boolean getIsVerified() { return isVerified != null && isVerified; }
    public void setIsVerified(Boolean isVerified) { this.isVerified = isVerified; }

    // ===== NEW GETTERS & SETTERS =====
    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }

    public String getShopLicenseUrl() { return shopLicenseUrl; }
    public void setShopLicenseUrl(String shopLicenseUrl) { this.shopLicenseUrl = shopLicenseUrl; }

    public String getGstNumber() { return gstNumber; }
    public void setGstNumber(String gstNumber) { this.gstNumber = gstNumber; }

    public String getShopPhotoUrl() { return shopPhotoUrl; }
    public void setShopPhotoUrl(String shopPhotoUrl) { this.shopPhotoUrl = shopPhotoUrl; }
}
