package com.khathabook.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "support_tickets")
public class SupportTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // COMPLAINT, PAYMENT_DISPUTE, SUPPORT, FRAUD_REPORT
    @Column(name = "ticket_type", nullable = false)
    private String ticketType;

    // OPEN, REVIEWING, RESOLVED, REJECTED
    @Column(nullable = false)
    private String status = "OPEN";

    @Column(length = 2000)
    private String description;

    @Column(name = "resolution_notes", length = 2000)
    private String resolutionNotes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    // The user who created this ticket. Bisa Customer atau Retailer.  
    // Untuk logik sederhana, kita simpan tipe user dan ID nya (polymorphic light)
    @Column(name = "reporter_type")
    private String reporterType; // CUSTOMER or RETAILER

    @Column(name = "reporter_id")
    private Long reporterId;

    // Target entitas yang dilaporkan, misal: Product ID kalau barang palsu, Order (Bill) ID kalau payment dispute
    @Column(name = "target_type")
    private String targetType; // PRODUCT, BILL, RETAILER, CUSTOMER

    @Column(name = "target_id")
    private Long targetId;

    public SupportTicket() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTicketType() { return ticketType; }
    public void setTicketType(String ticketType) { this.ticketType = ticketType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { 
        this.status = status; 
        this.updatedAt = LocalDateTime.now();
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public String getReporterType() { return reporterType; }
    public void setReporterType(String reporterType) { this.reporterType = reporterType; }

    public Long getReporterId() { return reporterId; }
    public void setReporterId(Long reporterId) { this.reporterId = reporterId; }

    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }

    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }
}
