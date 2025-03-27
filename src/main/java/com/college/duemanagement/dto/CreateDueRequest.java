package com.college.duemanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Data
public class CreateDueRequest {
    
    @NotNull
    private Long studentId;
    
    @NotBlank
    private String department;
    
    @NotBlank
    private String description;
    
    @NotNull
    @Positive
    private BigDecimal amount;
    
    @NotNull
    private LocalDateTime dueDate;
} 