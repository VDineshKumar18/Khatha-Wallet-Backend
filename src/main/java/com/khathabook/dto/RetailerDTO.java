package com.khathabook.dto;

public class RetailerDTO {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String shopName;
    private String upiId;
    private String payeeName;
    private Double latitude;
    private Double longitude;
    private Integer deliveryRadiusKm;
    
    private Double schemeTargetAmount;
    private Double schemeMonthlyAmount;

    private Boolean isVerified;
    private String approvalStatus;
    private String shopLicenseUrl;
    private String gstNumber;
    private String shopPhotoUrl;

    public RetailerDTO(Long id, String name, String email, String phone, String shopName, 
                       String upiId, String payeeName, Double latitude, Double longitude, 
                       Integer deliveryRadiusKm, Double schemeTargetAmount, Double schemeMonthlyAmount,
                       Boolean isVerified, String approvalStatus, String shopLicenseUrl, 
                       String gstNumber, String shopPhotoUrl) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.shopName = shopName;
        this.upiId = upiId;
        this.payeeName = payeeName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.deliveryRadiusKm = deliveryRadiusKm;
        this.schemeTargetAmount = schemeTargetAmount;
        this.schemeMonthlyAmount = schemeMonthlyAmount;
        this.isVerified = isVerified;
        this.approvalStatus = approvalStatus;
        this.shopLicenseUrl = shopLicenseUrl;
        this.gstNumber = gstNumber;
        this.shopPhotoUrl = shopPhotoUrl;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }

    public String getUpiId() { return upiId; }
    public void setUpiId(String upiId) { this.upiId = upiId; }

    public String getPayeeName() { return payeeName; }
    public void setPayeeName(String payeeName) { this.payeeName = payeeName; }
    
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

    public Boolean getIsVerified() { return isVerified; }
    public void setIsVerified(Boolean isVerified) { this.isVerified = isVerified; }

    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }

    public String getShopLicenseUrl() { return shopLicenseUrl; }
    public void setShopLicenseUrl(String shopLicenseUrl) { this.shopLicenseUrl = shopLicenseUrl; }

    public String getGstNumber() { return gstNumber; }
    public void setGstNumber(String gstNumber) { this.gstNumber = gstNumber; }

    public String getShopPhotoUrl() { return shopPhotoUrl; }
    public void setShopPhotoUrl(String shopPhotoUrl) { this.shopPhotoUrl = shopPhotoUrl; }
}
