package com.college.duemanagement.service.impl;

import com.college.duemanagement.dto.DepartmentSignatureDto;
import com.college.duemanagement.dto.NoDuesCertificateDto;
import com.college.duemanagement.entity.DepartmentSignature;
import com.college.duemanagement.entity.NoDuesCertificate;
import com.college.duemanagement.entity.Role;
import com.college.duemanagement.entity.User;
import com.college.duemanagement.exception.ResourceNotFoundException;
import com.college.duemanagement.repository.DepartmentSignatureRepository;
import com.college.duemanagement.repository.NoDuesCertificateRepository;
import com.college.duemanagement.repository.UserRepository;
import com.college.duemanagement.security.services.UserDetailsImpl;
import com.college.duemanagement.service.DueService;
import com.college.duemanagement.service.NoDuesCertificateService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NoDuesCertificateServiceImpl implements NoDuesCertificateService {

    private static final Logger logger = LoggerFactory.getLogger(NoDuesCertificateServiceImpl.class);

    private final NoDuesCertificateRepository certificateRepository;
    private final DepartmentSignatureRepository signatureRepository;
    private final UserRepository userRepository;
    private final DueService dueService;
    private final JavaMailSender mailSender;

    private final List<String> requiredDepartments = Arrays.asList(
            "LIBRARY", "TRAINING_AND_PLACEMENT", "SPORTS", "OFFICE", "HOD",
            "IES_LIBRARY", "TRANSPORT", "HOSTEL", "ACCOUNTS", "STUDENT_SECTION"
    );

    @Autowired
    public NoDuesCertificateServiceImpl(
            NoDuesCertificateRepository certificateRepository,
            DepartmentSignatureRepository signatureRepository,
            UserRepository userRepository,
            DueService dueService,
            JavaMailSender mailSender) {
        this.certificateRepository = certificateRepository;
        this.signatureRepository = signatureRepository;
        this.userRepository = userRepository;
        this.dueService = dueService;
        this.mailSender = mailSender;
    }

    @Override
    @Transactional
    public NoDuesCertificateDto createCertificate(String studentId) {
        User student = userRepository.findByUsername(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        List<NoDuesCertificate> existingCertificates = certificateRepository.findByStudent(student);
        if (!existingCertificates.isEmpty()) {
            NoDuesCertificate latestCertificate = existingCertificates.stream()
                    .filter(c -> c.getStatus() != NoDuesCertificate.CertificateStatus.REJECTED)
                    .max(Comparator.comparing(NoDuesCertificate::getCreatedAt))
                    .orElse(null);
            if (latestCertificate != null) {
                throw new RuntimeException("An active No Dues Certificate already exists: " + latestCertificate.getCertificateNumber());
            }
        }

        if (!isStudentEligibleForCertificate(studentId)) {
            throw new RuntimeException("Student has pending dues and is not eligible");
        }

        String certificateNumber = "IPS-" + student.getDepartment() + "-" + UUID.randomUUID().toString().substring(0, 8);

        NoDuesCertificate certificate = new NoDuesCertificate();
        certificate.setStudent(student);
        certificate.setCertificateNumber(certificateNumber);
        certificate.setStatus(NoDuesCertificate.CertificateStatus.PENDING);
        certificate.setPrincipalSigned(false);
        certificate.setCreatedAt(LocalDateTime.now());
        certificate.setUpdatedAt(LocalDateTime.now());

        List<DepartmentSignature> signatures = requiredDepartments.stream()
                .map(department -> {
                    DepartmentSignature signature = new DepartmentSignature();
                    signature.setStudent(student);
                    signature.setDepartment(department);
                    signature.setStatus(DepartmentSignature.SignatureStatus.PENDING);
                    return signature;
                })
                .collect(Collectors.toList());

        certificate.setDepartmentSignatures(signatures);
        NoDuesCertificate savedCertificate = certificateRepository.save(certificate);

        return NoDuesCertificateDto.fromEntity(savedCertificate);
    }

    @Override
    public NoDuesCertificateDto getCertificate(Long id) {
        NoDuesCertificate certificate = certificateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Certificate not found"));
        return NoDuesCertificateDto.fromEntity(certificate);
    }

    @Override
    public NoDuesCertificateDto getCertificateByNumber(String certificateNumber) {
        NoDuesCertificate certificate = certificateRepository.findByCertificateNumber(certificateNumber)
                .orElseThrow(() -> new RuntimeException("Certificate not found"));
        return NoDuesCertificateDto.fromEntity(certificate);
    }

    @Override
    public List<NoDuesCertificateDto> getCertificatesByStudent(String studentId) {
        User student = userRepository.findByUsername(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        return certificateRepository.findByStudent(student).stream()
                .map(NoDuesCertificateDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<NoDuesCertificateDto> getCertificatesByStatus(NoDuesCertificate.CertificateStatus status) {
        return certificateRepository.findByStatus(status).stream()
                .map(NoDuesCertificateDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<NoDuesCertificateDto> getPendingPrincipalSignatures() {
        return certificateRepository.findByPrincipalSigned(false).stream()
                .filter(certificate -> {
                    boolean allSigned = certificate.getDepartmentSignatures().stream()
                            .allMatch(s -> s.getStatus() == DepartmentSignature.SignatureStatus.SIGNED);
                    if (allSigned && certificate.getStatus() != NoDuesCertificate.CertificateStatus.ALLSGND && !certificate.getPrincipalSigned()) {
                        certificate.setStatus(NoDuesCertificate.CertificateStatus.ALLSGND);
                        certificate.setUpdatedAt(LocalDateTime.now());
                        certificateRepository.save(certificate);
                    }
                    return allSigned;
                })
                .map(NoDuesCertificateDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DepartmentSignatureDto signByDepartment(Long certificateId, String department, Long signedById, String comments, Boolean useESign) {
        NoDuesCertificate certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new RuntimeException("Certificate not found"));

        User signer = userRepository.findById(signedById)
                .orElseThrow(() -> new RuntimeException("Signer not found"));

        DepartmentSignature signature = certificate.getDepartmentSignatures().stream()
                .filter(s -> s.getDepartment().equals(department))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Signature not found for this department"));

        if (signature.getStatus() != DepartmentSignature.SignatureStatus.PENDING) {
            throw new RuntimeException("This department has already been signed or rejected");
        }

        signature.setSignedBy(signer.getFirstName() + " " + signer.getLastName() + " of "+signer.getDepartment());
        signature.setSignedAt(LocalDateTime.now());
        signature.setStatus(DepartmentSignature.SignatureStatus.SIGNED);
        signature.setComments(comments);

        if (Boolean.TRUE.equals(useESign)) {
            byte[] eSignatureBytes = signer.getESignature();
            if (eSignatureBytes != null) {
                // Convert byte[] to Base64 String
                signature.setESignature(eSignatureBytes); // Assuming setESignature accepts String
            } else {
                throw new RuntimeException("No eSignature found for user with ID: " + signedById);
            }
        }

        DepartmentSignature updatedSignature = signatureRepository.save(signature);

        long signedCount = certificate.getDepartmentSignatures().stream()
                .filter(s -> s.getStatus() == DepartmentSignature.SignatureStatus.SIGNED)
                .count();
        long totalCount = certificate.getDepartmentSignatures().size();

        if (signedCount == totalCount && !certificate.getPrincipalSigned()) {
            certificate.setStatus(NoDuesCertificate.CertificateStatus.ALLSGND);
        } else if (signedCount > 0 && signedCount < totalCount) {
            certificate.setStatus(NoDuesCertificate.CertificateStatus.PARTIAL);
        } else if (signedCount == 0) {
            certificate.setStatus(NoDuesCertificate.CertificateStatus.PENDING);
        }
        certificate.setUpdatedAt(LocalDateTime.now());
        certificateRepository.save(certificate);

        return DepartmentSignatureDto.fromEntity(updatedSignature);
    }

    @Override
    @Transactional
    public NoDuesCertificateDto signByPrincipal(Long certificateId, Long principalId, Boolean useESign) {
        NoDuesCertificate certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new RuntimeException("Certificate not found"));

        User principal = userRepository.findById(principalId)
                .orElseThrow(() -> new RuntimeException("Principal not found"));

        if (!isCertificateComplete(certificateId)) {
            throw new RuntimeException("Not all departments have signed the certificate");
        }

        certificate.setPrincipalSigned(true);
        certificate.setPrincipalSignedBy(principalId);
        certificate.setPrincipalSignedAt(LocalDateTime.now());
        certificate.setStatus(NoDuesCertificate.CertificateStatus.COMPLETE);
        certificate.setIssueDate(LocalDateTime.now());
        certificate.setUpdatedAt(LocalDateTime.now());

        if (Boolean.TRUE.equals(useESign)) {
            byte[] eSignatureBytes = principal.getESignature();
            if (eSignatureBytes != null) {
                certificate.setPrincipalESignature(eSignatureBytes); // Assuming setPrincipalESignature accepts String
            } else {
                throw new RuntimeException("No eSignature found for principal with ID: " + principalId);
            }
        }

        NoDuesCertificate updatedCertificate = certificateRepository.save(certificate);
        return NoDuesCertificateDto.fromEntity(updatedCertificate);
    }

    @Override
    @Transactional
    public void requestDepartmentSignature(Long certificateId, String department, Long hodId) throws Exception {
        NoDuesCertificate certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new EntityNotFoundException("Certificate not found with ID: " + certificateId));

        User hod = userRepository.findById(hodId)
                .orElseThrow(() -> new EntityNotFoundException("HOD not found with ID: " + hodId));

        List<DepartmentSignature> signatures = certificate.getDepartmentSignatures();
        if (signatures == null) {
            throw new IllegalStateException("No department signatures found for certificate: " + certificateId);
        }
        DepartmentSignature signature = signatures.stream()
                .filter(s -> department.equals(s.getDepartment()))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Signature not found for department: " + department));

        if (!DepartmentSignature.SignatureStatus.PENDING.equals(signature.getStatus())) {
            throw new IllegalStateException("Signature already processed for department: " + department);
        }

        User deptAdmin = userRepository.findByDepartmentAndRoles_Name(department, Role.ERole.ROLE_DEPARTMENT_ADMIN)
                .orElseThrow(() -> new EntityNotFoundException("No admin found for department: " + department));

        try {
            SimpleMailMessage message = getSimpleMailMessage(deptAdmin, hod, certificate);
            mailSender.send(message);
            logger.info("Signature request sent to {} for certificate {}", department, certificateId);
        } catch (MailException e) {
            logger.error("Failed to send signature request email: {}", e.getMessage());
        }

    }

    @Async
    private static SimpleMailMessage getSimpleMailMessage(User deptAdmin, User hod, NoDuesCertificate certificate) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(deptAdmin.getEmail());
        message.setSubject("Signature Request for No Dues Certificate");
        message.setText("Dear " + deptAdmin.getFirstName() + " " + deptAdmin.getLastName() + ",\n\n" +
                "The HOD (" + hod.getFirstName()+" "+hod.getLastName() + ") has requested your signature for No Dues Certificate " +
                certificate.getCertificateNumber() + " for student " + certificate.getStudent().getFirstName() + " "
                + certificate.getStudent().getLastName() +
                ". Please review and sign at your earliest convenience. \nYou can find the approval certificates on the 'Certificates' tab.\n\n" +

                "Best regards,\n" +
                "HOD\n" + hod.getFirstName() + " " + hod.getLastName());
        return message;
    }

    @Override
    public List<DepartmentSignatureDto> getPendingSignaturesByDepartment(String department) {
        return signatureRepository.findByDepartmentAndStatus(department, DepartmentSignature.SignatureStatus.PENDING)
                .stream()
                .map(DepartmentSignatureDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<DepartmentSignatureDto> getSignaturesByStudent(String studentId) {
        User student = userRepository.findByUsername(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        return signatureRepository.findByStudent(student).stream()
                .map(DepartmentSignatureDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isStudentEligibleForCertificate(String studentId) {
        return dueService.hasStudentClearedAllDues(studentId);
    }

    @Override
    public boolean isCertificateComplete(Long certificateId) {
        NoDuesCertificate certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new RuntimeException("Certificate not found"));
        boolean allSigned = certificate.getDepartmentSignatures().stream()
                .allMatch(s -> s.getStatus() == DepartmentSignature.SignatureStatus.SIGNED);
        if (allSigned && certificate.getStatus() != NoDuesCertificate.CertificateStatus.ALLSGND && !certificate.getPrincipalSigned()) {
            certificate.setStatus(NoDuesCertificate.CertificateStatus.ALLSGND);
            certificate.setUpdatedAt(LocalDateTime.now());
            certificateRepository.save(certificate);
        }
        return allSigned;
    }

    @Override
    public boolean hasStudentPendingDuesInDepartment(String studentId, String department) {
        User student = userRepository.findByUsername(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        return dueService.hasStudentPendingDuesInDepartment(student.getId(), department);
    }

    @Override
    public boolean hasStudentPendingDuesInDepartmentById(Long studentId, String department) {
        return dueService.hasStudentPendingDuesInDepartment(studentId, department);
    }

    @Override
    @Transactional
    public DepartmentSignatureDto generateDepartmentReceipt(String studentId, String department, Long signedById) {
        User student = userRepository.findByUsername(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        User signer = userRepository.findById(signedById)
                .orElseThrow(() -> new RuntimeException("Signer not found"));

        if (hasStudentPendingDuesInDepartment(studentId, department)) {
            throw new RuntimeException("Student has pending dues in " + department);
        }

        DepartmentSignature signature = signatureRepository.findByStudentAndDepartment(student, department)
                .stream().findFirst().orElse(new DepartmentSignature());
        signature.setStudent(student);
        signature.setDepartment(department);
        signature.setSignedBy(department+" "+signer.getRoles()+": "+signer.getFirstName()+" "+signer.getLastName());
        signature.setSignedAt(LocalDateTime.now());
        signature.setStatus(DepartmentSignature.SignatureStatus.SIGNED);
        signature.setComments("No dues in " + department);

        DepartmentSignature savedSignature = signatureRepository.save(signature);
        return DepartmentSignatureDto.fromEntity(savedSignature);
    }

    @Override
    @Transactional
    public DepartmentSignatureDto generateDepartmentReceiptById(Long studentId, String department, Long signedById) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        User signer = userRepository.findById(signedById)
                .orElseThrow(() -> new RuntimeException("Signer not found"));

        if (hasStudentPendingDuesInDepartmentById(studentId, department)) {
            throw new RuntimeException("Student has pending dues in " + department);
        }

        DepartmentSignature signature = signatureRepository.findByStudentAndDepartment(student, department)
                .stream().findFirst().orElse(new DepartmentSignature());
        signature.setStudent(student);
        signature.setDepartment(department);
        signature.setSignedBy(department+" "+signer.getRoles()+": "+signer.getFirstName()+" "+signer.getLastName());
        signature.setSignedAt(LocalDateTime.now());
        signature.setStatus(DepartmentSignature.SignatureStatus.SIGNED);
        signature.setComments("No dues in " + department);

        DepartmentSignature savedSignature = signatureRepository.save(signature);
        return DepartmentSignatureDto.fromEntity(savedSignature);
    }

    @Override
    public List<NoDuesCertificateDto> getStudentsWithClearedDues() {
        List<User> students = userRepository.findAll().stream()
                .filter(user -> user.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_STUDENT")))
                .collect(Collectors.toList());

        List<User> studentsWithClearedDues = students.stream()
                .filter(student -> dueService.hasStudentClearedAllDues(student.getUsername()))
                .collect(Collectors.toList());

        List<NoDuesCertificateDto> result = new ArrayList<>();
        for (User student : studentsWithClearedDues) {
            List<NoDuesCertificate> certificates = certificateRepository.findByStudent(student);
            if (!certificates.isEmpty()) {
                result.addAll(certificates.stream().map(NoDuesCertificateDto::fromEntity).collect(Collectors.toList()));
            } else {
                NoDuesCertificateDto dto = new NoDuesCertificateDto();
                dto.setStudentId(student.getId().toString());
                dto.setStudentName(student.getFirstName() + " " + student.getLastName());
                dto.setStatus("ELIGIBLE");
                result.add(dto);
            }
        }
        return result;
    }

    @Override
    public List<NoDuesCertificateDto> getStudentsWithClearedDuesByDepartment(String department) {
        List<User> students = userRepository.findAll().stream()
                .filter(user -> user.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_STUDENT")))
                .filter(user -> department.equals(user.getDepartment()))
                .toList();

        List<User> studentsWithClearedDues = students.stream()
                .filter(student -> dueService.hasStudentClearedAllDues(student.getUsername()))
                .toList();

        List<NoDuesCertificateDto> result = new ArrayList<>();
        for (User student : studentsWithClearedDues) {
            List<NoDuesCertificate> certificates = certificateRepository.findByStudent(student);
            if (!certificates.isEmpty()) {
                result.addAll(certificates.stream().map(NoDuesCertificateDto::fromEntity).collect(Collectors.toList()));
            } else {
                NoDuesCertificateDto dto = new NoDuesCertificateDto();
                dto.setStudentId(student.getId().toString());
                dto.setStudentName(student.getFirstName() + " " + student.getLastName());
                dto.setStatus("ELIGIBLE");
                result.add(dto);
            }
        }
        return result;
    }

    @Override
    public List<NoDuesCertificateDto> getAllCertificates() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (userDetails == null) {
            throw new RuntimeException("Not authenticated");
        }
        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return certificateRepository.findAll().stream()
                    .map(NoDuesCertificateDto::fromEntity)
                    .collect(Collectors.toList());
        } else if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_HOD") || a.getAuthority().equals("ROLE_PRINCIPAL"))) {
            return certificateRepository.findByStudentDepartment(userDetails.getDepartment()).stream()
                    .map(NoDuesCertificateDto::fromEntity)
                    .collect(Collectors.toList());
        }

        else if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_DEPARTMENT_ADMIN"))) {
            return certificateRepository.findAll().stream()
                    .filter(certificate -> certificate.getDepartmentSignatures().stream()
                            .anyMatch(signature -> signature.getDepartment().equals(userDetails.getDepartment())
                                    && (signature.getStatus() == DepartmentSignature.SignatureStatus.PENDING)))
                    .map(NoDuesCertificateDto::fromEntity)
                    .collect(Collectors.toList());
        }

        else if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"))) {
            User student = userRepository.findById(userDetails.getId())

                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));
            return certificateRepository.findByStudent(student).stream()
                    .map(NoDuesCertificateDto::fromEntity)
                    .collect(Collectors.toList());
        } else {
            throw new RuntimeException("Unauthorized: Insufficient permissions");
        }
    }

    @Override
    public NoDuesCertificateDto updateCertificateStatus(Long id, String requested) {
        if (id == null || requested == null || requested.trim().isEmpty()) {
            throw new IllegalArgumentException("Certificate ID and status are required");
        }

        NoDuesCertificate certificate = certificateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found with ID: " + id));

        NoDuesCertificate.CertificateStatus newStatus;
        switch (requested.toUpperCase()) {
            case "PENDING": newStatus = NoDuesCertificate.CertificateStatus.PENDING; break;
            case "PARTIAL": newStatus = NoDuesCertificate.CertificateStatus.PARTIAL; break;
            case "ALLSIGNED": newStatus = NoDuesCertificate.CertificateStatus.ALLSGND; break;
            case "COMPLETE": newStatus = NoDuesCertificate.CertificateStatus.COMPLETE; break;
            case "REJECTED": newStatus = NoDuesCertificate.CertificateStatus.REJECTED; break;
            default: throw new IllegalArgumentException("Invalid status: " + requested);
        }

        if (!isValidTransition(certificate.getStatus(), newStatus)) {
            throw new IllegalStateException("Cannot transition from " + certificate.getStatus() + " to " + newStatus);
        }

        if (newStatus == NoDuesCertificate.CertificateStatus.COMPLETE) {
            if (certificate.getStatus() != NoDuesCertificate.CertificateStatus.ALLSGND) {
                throw new IllegalStateException("Cannot transition to COMP: All departments must sign first");
            }
            boolean allDepartmentsSigned = certificate.getDepartmentSignatures().stream()
                    .allMatch(s -> s.getStatus() == DepartmentSignature.SignatureStatus.SIGNED);
            if (!allDepartmentsSigned) {
                throw new IllegalStateException("Cannot transition to COMP: Not all departments have signed");
            }
        }

        certificate.setStatus(newStatus);
        certificate.setUpdatedAt(LocalDateTime.now());

        if (newStatus == NoDuesCertificate.CertificateStatus.COMPLETE) {
            UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (userDetails == null) {
                throw new RuntimeException("Not authenticated");
            }
            certificate.setPrincipalSigned(true);
            certificate.setPrincipalSignedBy(userDetails.getId());
            certificate.setPrincipalSignedAt(LocalDateTime.now());
            if (certificate.getIssueDate() == null) {
                certificate.setIssueDate(LocalDateTime.now());
            }
        }

        NoDuesCertificate updatedCertificate = certificateRepository.save(certificate);
        return NoDuesCertificateDto.fromEntity(updatedCertificate);
    }

    private boolean isValidTransition(NoDuesCertificate.CertificateStatus currentStatus, NoDuesCertificate.CertificateStatus newStatus) {
        switch (currentStatus) {
            case PENDING: return newStatus == NoDuesCertificate.CertificateStatus.PARTIAL || newStatus == NoDuesCertificate.CertificateStatus.REJECTED;
            case PARTIAL: return newStatus == NoDuesCertificate.CertificateStatus.ALLSGND || newStatus == NoDuesCertificate.CertificateStatus.REJECTED;
            case ALLSGND: return newStatus == NoDuesCertificate.CertificateStatus.COMPLETE || newStatus == NoDuesCertificate.CertificateStatus.REJECTED;
            case COMPLETE:
            case REJECTED: return false;
            default: return false;
        }
    }

    @Override
    @Transactional
    public void deleteCertificate(Long id) {
        NoDuesCertificate certificate = certificateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found with ID: " + id));

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (userDetails == null) {
            throw new RuntimeException("Not authenticated");
        }

        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            throw new RuntimeException("Unauthorized: Only admins can delete certificates");
        }

        List<DepartmentSignature> signatures = certificate.getDepartmentSignatures();
        if (signatures != null && !signatures.isEmpty()) {
            signatureRepository.deleteAll(signatures);
        }

        certificateRepository.delete(certificate);
        logger.info("Certificate with ID {} deleted by user {}", id, userDetails.getUsername());
    }
}