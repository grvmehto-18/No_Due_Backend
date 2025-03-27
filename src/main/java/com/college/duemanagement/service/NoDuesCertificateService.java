package com.college.duemanagement.service;

import com.college.duemanagement.dto.DepartmentSignatureDto;
import com.college.duemanagement.dto.NoDuesCertificateDto;
import com.college.duemanagement.entity.NoDuesCertificate;

import java.util.List;

public interface NoDuesCertificateService {

    // Certificate management
    NoDuesCertificateDto createCertificate(String studentId);
    NoDuesCertificateDto getCertificate(Long id);
    NoDuesCertificateDto getCertificateByNumber(String certificateNumber);
    List<NoDuesCertificateDto> getCertificatesByStudent(String studentId);
    List<NoDuesCertificateDto> getCertificatesByStatus(NoDuesCertificate.CertificateStatus status);
    List<NoDuesCertificateDto> getPendingPrincipalSignatures();
    NoDuesCertificateDto signByPrincipal(Long certificateId, Long principalId, Boolean useESign);

    // Department signatures
    DepartmentSignatureDto signByDepartment(Long certificateId, String department, Long signedById, String comments, Boolean useESign);
    List<DepartmentSignatureDto> getPendingSignaturesByDepartment(String department);
    List<DepartmentSignatureDto> getSignaturesByStudent(String studentId);
    void requestDepartmentSignature(Long certificateId, String department, Long hodId) throws Exception; // New method

    // Check if student has all dues cleared
    boolean isStudentEligibleForCertificate(String studentId);

    // Check if certificate is complete (all departments signed)
    boolean isCertificateComplete(Long certificateId);

    // Department-specific receipt methods
    boolean hasStudentPendingDuesInDepartment(String studentId, String department);
    DepartmentSignatureDto generateDepartmentReceipt(String studentId, String department, Long signedById);

    // Department-specific receipt methods with Long studentId
    boolean hasStudentPendingDuesInDepartmentById(Long studentId, String department);
    DepartmentSignatureDto generateDepartmentReceiptById(Long studentId, String department, Long signedById);

    // Get all students with cleared dues for HOD
    List<NoDuesCertificateDto> getStudentsWithClearedDues();

    // Get students from a specific department with cleared dues
    List<NoDuesCertificateDto> getStudentsWithClearedDuesByDepartment(String department);

    List<NoDuesCertificateDto> getAllCertificates();

    NoDuesCertificateDto updateCertificateStatus(Long id, String requested);

    void deleteCertificate(Long id);
}