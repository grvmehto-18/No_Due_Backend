package com.college.duemanagement.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class NoDueCertificateDto {
    
    private Long id;
    private Long studentId;
    private String studentName;
    private String studentRollNumber;
    private String certificateNumber;
    private LocalDateTime issueDate;
    private String status;
    private Map<String, Boolean> departmentApprovals;
    private Boolean hodApproved;
    private Boolean principalApproved;
    private String certificatePath;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 