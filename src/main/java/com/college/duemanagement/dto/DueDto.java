package com.college.duemanagement.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DueDto {
    
    private Long id;
    private Long studentId;
    private String rollNumber;
    private String studentName;
    private String department;
    private String description;
    private BigDecimal amount;
    private LocalDateTime dueDate;
    private String paymentStatus;
    private LocalDateTime paymentDate;
    private String paymentReference;
    private Long approvedBy;
    private String approvedByName;
    private LocalDateTime approvalDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean receiptGenerated;
    private String receiptNumber; // New field
} 