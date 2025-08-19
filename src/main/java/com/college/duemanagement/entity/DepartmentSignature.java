package com.college.duemanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "department_signatures")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentSignature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(name = "department", nullable = false)
    private String department;

    @Column(name = "signed_by")
    private String signedBy;

    @Column(name = "signed_at")
    private LocalDateTime signedAt;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private SignatureStatus status = SignatureStatus.PENDING;

    @Column(name = "comments")
    private String comments;

    @Column(name = "e_signature", columnDefinition = "LONGBLOB", length = 16777215)
    @Lob
    private byte[] eSignature;

    @ManyToOne
    @JoinColumn(name = "certificate_id")
    private NoDuesCertificate certificate;

    public enum SignatureStatus {
        PENDING,
        SIGNED,
        REJECTED
    }
}