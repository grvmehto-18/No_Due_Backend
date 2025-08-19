package com.college.duemanagement.dto;

import com.college.duemanagement.entity.NoDuesCertificate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoDuesCertificateDto {
    private Long id;
    private String studentId;
    private String studentName;
    private String studentRollNumber; // Add roll number explicitly
    private String branch; // Add branch
    private int semester; // Add semester
    private String computerCode;
    private String email; // Add email
    private String mobileNumber; // Add mobile number
    private String certificateNumber;
    private LocalDateTime issueDate;
    private String status;
    private Boolean principalSigned;
    private String principalSignedBy;
    private LocalDateTime principalSignedAt;
    private LocalDateTime createdAt;
    private List<DepartmentSignatureDto> departmentSignatures;
    private String principalESignature;

    public static NoDuesCertificateDto fromEntity(NoDuesCertificate entity) {
        NoDuesCertificateDto dto = new NoDuesCertificateDto();
        dto.setId(entity.getId());
        dto.setStudentId(entity.getStudent().getStudent().getRollNumber());
        dto.setStudentName(entity.getStudent().getFirstName() + " " + entity.getStudent().getLastName());
        dto.setStudentRollNumber(entity.getStudent().getStudent().getRollNumber());
        dto.setComputerCode(entity.getStudent().getUniqueCode());
        dto.setBranch(entity.getStudent().getDepartment());
        dto.setSemester(entity.getStudent().getStudent().getSemester());
        dto.setEmail(entity.getStudent().getEmail());
        dto.setMobileNumber(entity.getStudent().getStudent().getContactNumber());
        dto.setCertificateNumber(entity.getCertificateNumber());
        dto.setIssueDate(entity.getIssueDate());
        dto.setStatus(entity.getStatus().name());
        dto.setPrincipalSigned(entity.getPrincipalSigned());
        dto.setPrincipalSignedAt(entity.getPrincipalSignedAt());
        dto.setCreatedAt(entity.getCreatedAt());

        if (entity.getDepartmentSignatures() != null) {
            dto.setDepartmentSignatures(
                    entity.getDepartmentSignatures().stream()
                            .map(DepartmentSignatureDto::fromEntity)
                            .collect(Collectors.toList())
            );
        }
        if (entity.getPrincipalESignature() != null) {
            dto.setPrincipalESignature(Base64.getEncoder().encodeToString(entity.getPrincipalESignature()));
        }

        return dto;
    }
}