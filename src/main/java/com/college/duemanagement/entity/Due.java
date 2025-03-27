package com.college.duemanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "dues")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Due {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;
    
    @Column(name = "department", nullable = false)
    private String department;
    
    @Column(name = "description", nullable = false)
    private String description;
    
    @Column(name = "amount", nullable = false)
    private BigDecimal amount;
    
    @Column(name = "due_date", nullable = false)
    private LocalDateTime dueDate;
    
    @Column(name = "payment_status")
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;
    
    @Column(name = "payment_date")
    private LocalDateTime paymentDate;
    
    @Column(name = "payment_reference")
    private String paymentReference;
    
    @Column(name = "approved_by")
    private Long approvedBy;
    
    @Column(name = "approval_date")
    private LocalDateTime approvalDate;
    
    @Column(name = "created_by", nullable = false)
    private Long createdBy;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "receipt_generated", nullable = false)
    private boolean receiptGenerated = false;

    @Column(unique = true) // Ensure uniqueness in the database
    private String receiptNumber; // New field for unique receipt number
    
    public enum PaymentStatus {
        PENDING,
        PAID,
        APPROVED,
        REJECTED
    }
} 