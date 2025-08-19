package com.college.duemanagement.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DepartmentDto {
    
    private Long id;
    private String name;
    private String code;
    private String description;
    private Long hodId;
    private String hodName;
    private Long adminId;
    private String adminName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 