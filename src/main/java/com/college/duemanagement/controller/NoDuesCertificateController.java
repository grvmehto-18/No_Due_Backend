package com.college.duemanagement.controller;

import com.college.duemanagement.dto.DepartmentSignatureDto;
import com.college.duemanagement.dto.NoDuesCertificateDto;
import com.college.duemanagement.entity.NoDuesCertificate;
import com.college.duemanagement.entity.Student;
import com.college.duemanagement.security.services.UserDetailsImpl;
import com.college.duemanagement.service.NoDuesCertificateService;
import com.college.duemanagement.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/certificates")
@Tag(name = "No Dues Certificate", description = "APIs for managing no-dues certificates")
public class NoDuesCertificateController {

    private static final Logger logger = LoggerFactory.getLogger(NoDuesCertificateController.class);

    private final NoDuesCertificateService certificateService;
    private final StudentService studentService;

    @Autowired
    public NoDuesCertificateController(NoDuesCertificateService certificateService, StudentService studentService) {
        this.certificateService = certificateService;
        this.studentService = studentService;
    }

    @GetMapping
    @Operation(summary = "Get all no-dues certificates")
    public ResponseEntity<?> getAllCertificates() {
        try {
            List<NoDuesCertificateDto> certificates = certificateService.getAllCertificates();
            return ResponseEntity.ok(certificates);
        } catch (Exception e) {
            logger.error("Error fetching all certificates: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Error fetching all certificates: " + e.getMessage())
            );
        }
    }

    @PostMapping("/request/{rollNumber}")
    @Operation(summary = "Request a no-dues certificate")
    public ResponseEntity<?> requestCertificate(@PathVariable String rollNumber) {
        logger.info("Student requesting certificate for roll number: {}", rollNumber);
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (userDetails == null) {
                logger.error("No authenticated user found");
                return ResponseEntity.status(401).body(Map.of("message", "Not authenticated", "status", "error"));
            }

            Student student = studentService.getStudentByRollNumber(rollNumber);
            if (student == null) {
                logger.error("No student found with roll number: {}", rollNumber);
                return ResponseEntity.badRequest().body(Map.of("message", "No student found with roll number: " + rollNumber, "status", "error"));
            }

            String studentUsername = student.getUser().getUsername();
            if (!studentUsername.equals(userDetails.getUsername())) {
                logger.warn("Roll number {} does not match user {}", rollNumber, userDetails.getUsername());
                return ResponseEntity.status(403).body(Map.of("message", "You can only request for your own roll number", "status", "error"));
            }

            if (!certificateService.isStudentEligibleForCertificate(studentUsername)) {
                logger.warn("Student {} not eligible due to pending dues", rollNumber);
                return ResponseEntity.badRequest().body(Map.of("message", "You have pending dues. Clear them before requesting a certificate.", "status", "error"));
            }

            List<NoDuesCertificateDto> existingCertificates = certificateService.getCertificatesByStudent(studentUsername);
            NoDuesCertificateDto certificate = existingCertificates.isEmpty() ?
                    certificateService.createCertificate(studentUsername) :
                    existingCertificates.get(0);

            // Use String comparison for DTO status
            if ("COMPLETE".equals(certificate.getStatus())) {
                logger.info("Student {} already has an issued certificate", rollNumber);
                return ResponseEntity.ok(Map.of("message", "You already have an issued certificate", "certificate", certificate, "status", "success"));
            }

//            certificate = certificateService.updateCertificateStatus(certificate.getId(), String.valueOf(NoDuesCertificate.CertificateStatus.PENDING));
            logger.info("Certificate set to PEND for roll number {}", rollNumber);
            return ResponseEntity.ok(Map.of("message", "Certificate request submitted. Awaiting approval.", "certificate", certificate, "status", "success"));
        } catch (Exception e) {
            logger.error("Error processing request for {}: {}", rollNumber, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", "Error requesting certificate: " + e.getMessage(), "status", "error"));
        }
    }

    @PostMapping("/student/{studentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HOD')")
    @Operation(summary = "Create a no-dues certificate for a student")
    public ResponseEntity<?> createCertificate(@PathVariable String studentId) {
        logger.info("Creating certificate for student ID: {}", studentId);
        try {
            if (!certificateService.isStudentEligibleForCertificate(studentId)) {
                logger.warn("Student {} not eligible for certificate", studentId);
                return ResponseEntity.badRequest().body(Map.of("message", "Student has pending dues. Clear them first."));
            }
            NoDuesCertificateDto certificate = certificateService.createCertificate(studentId);
            logger.info("Certificate created for student ID: {}", studentId);
            return ResponseEntity.ok(certificate);
        } catch (Exception e) {
            logger.error("Error creating certificate for {}: {}.", studentId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", "Error creating certificate: " + e.getMessage()));
        }
    }

    @PostMapping("/student/roll/{rollNumber}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HOD')")
    @Operation(summary = "Create a no-dues certificate by roll number")
    public ResponseEntity<?> createCertificateByRollNumber(@PathVariable String rollNumber) {
        logger.info("Creating certificate for roll number: {}", rollNumber);
        try {
            Student student = studentService.getStudentByRollNumber(rollNumber);
            if (student == null) {
                logger.error("No student found with roll number: {}.", rollNumber);
                return ResponseEntity.badRequest().body(Map.of("message", "No student found with roll number: " + rollNumber));
            }
            String username = student.getUser().getUsername();
            if (!certificateService.isStudentEligibleForCertificate(username)) {
                logger.warn("Student {} not eligible", username);
                return ResponseEntity.badRequest().body(Map.of("message", "Student has pending dues. Clear them first."));
            }
            NoDuesCertificateDto certificate = certificateService.createCertificate(username);
            logger.info("Certificate created for roll number: {}", rollNumber);
            return ResponseEntity.ok(certificate);
        } catch (Exception e) {
            logger.error("Error creating certificate for {}: {}", rollNumber, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", "Error creating certificate: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DEPARTMENT_ADMIN') or hasRole('HOD') or hasRole('PRINCIPAL') or hasRole('STUDENT')")
    @Operation(summary = "Get a no-dues certificate by ID")
    public ResponseEntity<NoDuesCertificateDto> getCertificate(@PathVariable Long id) {
        return ResponseEntity.ok(certificateService.getCertificate(id));
    }

    @GetMapping("/number/{certificateNumber}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DEPARTMENT_ADMIN') or hasRole('HOD') or hasRole('PRINCIPAL') or hasRole('STUDENT')")
    @Operation(summary = "Get a no-dues certificate by certificate number")
    public ResponseEntity<NoDuesCertificateDto> getCertificateByNumber(@PathVariable String certificateNumber) {
        return ResponseEntity.ok(certificateService.getCertificateByNumber(certificateNumber));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DEPARTMENT_ADMIN') or hasRole('HOD') or hasRole('PRINCIPAL') or @userSecurity.isCurrentUser(#studentId)")
    @Operation(summary = "Get all no-dues certificates for a student")
    public ResponseEntity<List<NoDuesCertificateDto>> getCertificatesByStudent(@PathVariable String studentId) {
        return ResponseEntity.ok(certificateService.getCertificatesByStudent(studentId));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HOD') or hasRole('PRINCIPAL')")
    @Operation(summary = "Get no-dues certificates by status")
    public ResponseEntity<List<NoDuesCertificateDto>> getCertificatesByStatus(@PathVariable String status) {
        NoDuesCertificate.CertificateStatus certificateStatus = switch (status.toUpperCase()) {
            case "PENDING" -> NoDuesCertificate.CertificateStatus.PENDING;
            case "PARTIAL" -> NoDuesCertificate.CertificateStatus.PARTIAL;
            case "ALLSIGNED" -> NoDuesCertificate.CertificateStatus.ALLSGND;
            case "COMPLETE" -> NoDuesCertificate.CertificateStatus.COMPLETE;
            case "REJECTED" -> NoDuesCertificate.CertificateStatus.REJECTED;
            default -> throw new IllegalArgumentException("Invalid status: " + status);
        };
        return ResponseEntity.ok(certificateService.getCertificatesByStatus(certificateStatus));
    }

    @GetMapping("/pending-principal")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PRINCIPAL')")
    @Operation(summary = "Get certificates pending principal signature")
    public ResponseEntity<List<NoDuesCertificateDto>> getPendingPrincipalSignatures() {
        return ResponseEntity.ok(certificateService.getPendingPrincipalSignatures());
    }

    @PostMapping("/{id}/sign-principal")
    @PreAuthorize("hasRole('PRINCIPAL')or hasRole('ADMIN')")
    @Operation(summary = "Sign a certificate by the principal")
    public ResponseEntity<NoDuesCertificateDto> signByPrincipal(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "true") Boolean useESign,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            if (userDetails.getAuthorities().equals("ROLE_PRINCIPAL")) {
                return ResponseEntity.ok(certificateService.signByPrincipal(id, userDetails.getId(), useESign));
            }
            return ResponseEntity.ok(certificateService.signByPrincipal(id, userDetails.getId(), useESign));


        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/{id}/sign-department")
    @PreAuthorize("hasRole('DEPARTMENT_ADMIN') or hasRole('ADMIN') or hasRole('HOD')")
    @Operation(summary = "Sign a certificate by a department")
    public ResponseEntity<DepartmentSignatureDto> signByDepartment(
            @PathVariable Long id,
            @RequestParam String department,
            @RequestParam(required = false) String comments,
            @RequestParam(required = false, defaultValue = "true") Boolean useESign,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(certificateService.signByDepartment(id, department, userDetails.getId(), comments, useESign));
    }

    @PostMapping("/{id}/request-department-signature")
    @PreAuthorize("hasRole('DEPARTMENT_ADMIN') or hasRole('HOD')")
    @Operation(summary = "Request a signature from a department")
    public ResponseEntity<?> requestDepartmentSignature(
            @PathVariable Long id,
            @RequestParam String department) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        logger.info("ID from certificate request: {} and path variable id: {}",userDetails.getId(),id);
        try {
            certificateService.requestDepartmentSignature(id, department, userDetails.getId());
            logger.info("Signature request sent by HOD {} to department {} for certificate {}", userDetails.getUsername(), department, id);
            return ResponseEntity.ok(Map.of("message", "Signature request sent to " + department));
        } catch (Exception e) {
            logger.error("Error requesting signature: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", "Error requesting signature: " + e.getMessage()));
        }
    }

    @GetMapping("/pending-signatures/{department}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DEPARTMENT_ADMIN') or hasRole('HOD')")
    @Operation(summary = "Get pending signatures for a department")
    public ResponseEntity<List<DepartmentSignatureDto>> getPendingSignaturesByDepartment(@PathVariable String department) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_DEPARTMENT_ADMIN")) &&
                !department.equals(userDetails.getDepartment())) {
            return ResponseEntity.badRequest().body(null);
        }
        return ResponseEntity.ok(certificateService.getPendingSignaturesByDepartment(department));
    }

    @GetMapping("/check-eligibility/{studentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DEPARTMENT_ADMIN') or hasRole('HOD') or hasRole('PRINCIPAL') or @userSecurity.isCurrentUser(#studentId)")
    @Operation(summary = "Check if a student is eligible for a no-dues certificate")
    public ResponseEntity<Boolean> checkEligibility(@PathVariable String studentId) {
        return ResponseEntity.ok(certificateService.isStudentEligibleForCertificate(studentId));
    }

    @GetMapping("/students-with-cleared-dues")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HOD') or hasRole('PRINCIPAL')")
    @Operation(summary = "Get students with all dues cleared")
    public ResponseEntity<List<NoDuesCertificateDto>> getStudentsWithClearedDues() {
        logger.info("Fetching students with cleared dues");
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            boolean isHOD = userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_HOD"));
            List<NoDuesCertificateDto> certificates = isHOD ?
                    certificateService.getStudentsWithClearedDuesByDepartment(userDetails.getDepartment()) :
                    certificateService.getStudentsWithClearedDues();
            logger.info("Found {} students with cleared dues", certificates.size());
            return ResponseEntity.ok(certificates);
        } catch (Exception e) {
            logger.error("Error fetching students with cleared dues: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/department-receipt/{studentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DEPARTMENT_ADMIN') or hasRole('HOD')")
    @Operation(summary = "Generate a department-specific receipt")
    public ResponseEntity<?> generateDepartmentReceipt(
            @PathVariable Long studentId,
            @RequestParam String department) {
        logger.info("Generating receipt for student {} in department {}", studentId, department);
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_DEPARTMENT_ADMIN")) &&
                    !department.equals(userDetails.getDepartment())) {
                logger.warn("Department admin {} attempted receipt for wrong department {}", userDetails.getUsername(), department);
                return ResponseEntity.badRequest().body(Map.of("message", "You can only generate receipts for your own department", "status", "error"));
            }

            if (certificateService.hasStudentPendingDuesInDepartmentById(studentId, department)) {
                logger.warn("Student {} has pending dues in {}", studentId, department);
                return ResponseEntity.badRequest().body(Map.of("message", "Student has pending dues in this department", "status", "error"));
            }

            DepartmentSignatureDto receipt = certificateService.generateDepartmentReceiptById(studentId, department, userDetails.getId());
            logger.info("Receipt generated for student {} in {}", studentId, department);
            return ResponseEntity.ok(receipt);
        } catch (Exception e) {
            logger.error("Error generating receipt for {} in {}: {}", studentId, department, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", "Error generating receipt: " + e.getMessage(), "status", "error"));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a no-dues certificate")
    public ResponseEntity<Void> deleteCertificate(@PathVariable Long id) {
        certificateService.deleteCertificate(id);
        return ResponseEntity.noContent().build();
    }
}