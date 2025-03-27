package com.college.duemanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private ERole name;
    
    public enum ERole {
        ROLE_STUDENT,
        ROLE_DEPARTMENT_ADMIN,
        ROLE_HOD,
        ROLE_PRINCIPAL,
        ROLE_ADMIN
    }
} 