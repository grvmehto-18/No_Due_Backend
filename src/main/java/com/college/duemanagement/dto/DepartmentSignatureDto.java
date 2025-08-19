package com.college.duemanagement.dto;

import com.college.duemanagement.entity.DepartmentSignature;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Base64;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentSignatureDto {
    private Long id;
    private String studentId;
    private String studentName;
    private String department;
    private String signedBy;
    private LocalDateTime signedAt;
    private String status;
    private String comments;
    private String eSignature;
    
    public static DepartmentSignatureDto fromEntity(DepartmentSignature entity) {
        DepartmentSignatureDto dto = new DepartmentSignatureDto();
        dto.setId(entity.getId());
        dto.setStudentId(entity.getStudent().getUsername());
        dto.setSignedBy(entity.getSignedBy());
        dto.setStudentName(entity.getStudent().getFirstName() + " " + entity.getStudent().getLastName());
        dto.setDepartment(entity.getDepartment());
        dto.setSignedAt(entity.getSignedAt());
        dto.setStatus(entity.getStatus().name());
        dto.setComments(entity.getComments());
        if (entity.getESignature() != null) {
            dto.setESignature(Base64.getEncoder().encodeToString(entity.getESignature()));
        }
        return dto;
    }
} 