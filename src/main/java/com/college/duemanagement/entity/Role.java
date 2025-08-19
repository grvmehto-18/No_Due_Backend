package com.college.duemanagement.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "roles")
@Data
public class Role {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private ERole name;

    public Role() {
    }

    public Role(ERole name) {
        this.name = name;
    }
    
    public enum ERole {
        ROLE_STUDENT,
        ROLE_DEPARTMENT_ADMIN,
        ROLE_HOD,
        ROLE_PRINCIPAL,
        ROLE_ADMIN
    }
} 