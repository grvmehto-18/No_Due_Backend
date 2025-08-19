package com.college.duemanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "no_dues_certificates")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoDuesCertificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(name = "certificate_number", unique = true)
    private String certificateNumber;

    @Column(name = "issue_date")
    private LocalDateTime issueDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CertificateStatus status = CertificateStatus.PENDING;

    @Column(name = "principal_signed")
    private Boolean principalSigned = false;

    @Column(name = "principal_signed_by")
    private Long principalSignedBy;

    @Column(name = "principal_signed_at")
    private LocalDateTime principalSignedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "certificate_id")
    private List<DepartmentSignature> departmentSignatures;

    @Column(name = "principal_e_signature", columnDefinition = "LONGBLOB", length = 16777215)
    @Lob
    private byte[] principalESignature;


    public enum CertificateStatus {
        PENDING,    // 4 chars
        PARTIAL,    // 4 chars
        ALLSGND, // 7 chars
        COMPLETE,    // 4 chars
        REJECTED
    }


}