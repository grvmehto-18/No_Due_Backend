package com.college.duemanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "students")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Student {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ToString.Exclude
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "roles", "resetToken", "resetTokenExpiry", "isAccountNonExpired", "isAccountNonLocked", "isCredentialsNonExpired", "isEnabled", "eSignature"})
    private User user;
    
    @Column(nullable = false, unique = true)
    private String rollNumber;
    
    @Column(nullable = false)
    private Integer semester;
    
    @Column(nullable = false)
    private String batch;
    
    @Column(nullable = false)
    private String course;
    
    @Column(nullable = false)
    private String section;
    
    @Column(name = "father_name", nullable = false)
    private String fatherName;
    
    @Column(name = "mother_name", nullable = false)
    private String motherName;
    
    @Column(name = "contact_number", nullable = false)
    private String contactNumber;
    
    @Column(nullable = false)
    private String address;
    
    @Column(name = "created_at", nullable = false)
    private java.time.LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private java.time.LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = java.time.LocalDateTime.now();
    }
} 