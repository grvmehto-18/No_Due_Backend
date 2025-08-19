package com.college.duemanagement.dto;

import lombok.Data;
import java.util.Set;

@Data
public class UserDto {
    
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String uniqueCode;
    private Set<String> roles;
    private String department;
    private boolean hasESignature;
    private String eSignature; // Base64 encoded signature
} 