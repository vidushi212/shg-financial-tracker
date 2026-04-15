package com.shg.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String type;
    
    @Column(nullable = false)
    private Double amount;
    
    @Column(length = 500)
    private String description;
    
    @Column(nullable = false)
    private String recordedBy;
    
    @Column(nullable = false)
    private LocalDateTime transactionDate = LocalDateTime.now();
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @Column(nullable = false)
    private String state = "PENDING";  // PENDING, APPROVED, REJECTED
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shg_group_id", nullable = false)
    private SHGGroup shgGroup;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private SHGMember member;

    // Constructors
    public Transaction() {}

    public Transaction(String type, Double amount, String recordedBy, SHGGroup shgGroup) {
        this.type = type;
        this.amount = amount;
        this.recordedBy = recordedBy;
        this.shgGroup = shgGroup;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRecordedBy() { return recordedBy; }
    public void setRecordedBy(String recordedBy) { this.recordedBy = recordedBy; }

    public LocalDateTime getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDateTime transactionDate) { this.transactionDate = transactionDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public SHGGroup getShgGroup() { return shgGroup; }
    public void setShgGroup(SHGGroup shgGroup) { this.shgGroup = shgGroup; }

    public SHGMember getMember() { return member; }
    public void setMember(SHGMember member) { this.member = member; }
}