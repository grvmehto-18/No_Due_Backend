package com.college.duemanagement.payload.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateStudentRequest {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    @NotBlank(message = "First name is required")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    private String lastName;
    
    @NotBlank(message = "Department is required")
    private String department;
    
    @NotBlank(message = "Roll number is required")
    private String rollNumber;
    
    @NotNull(message = "Semester is required")
    private Integer semester;
    
    @NotBlank(message = "Batch is required")
    private String batch;
    
    @NotBlank(message = "Course is required")
    private String course;
    
    @NotBlank(message = "Section is required")
    private String section;
    
    @NotBlank(message = "Father's name is required")
    private String fatherName;
    
    @NotBlank(message = "Mother's name is required")
    private String motherName;
    
    @NotBlank(message = "Contact number is required")
    @Size(min = 10, max = 10, message = "Contact number must be 10 digits")
    private String contactNumber;
    
    @NotBlank(message = "Address is required")
    private String address;
} 