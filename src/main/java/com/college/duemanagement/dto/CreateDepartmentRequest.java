package com.college.duemanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateDepartmentRequest {
    
    @NotBlank
    @Size(min = 2, max = 100)
    private String name;
    
    @NotBlank
    @Size(min = 2, max = 20)
    private String code;
    
    private String description;
    
    private Long hodId;
    
    private Long adminId;
} 