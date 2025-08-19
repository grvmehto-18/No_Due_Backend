package com.college.duemanagement.controller;

import com.college.duemanagement.dto.CreateDueRequest;
import com.college.duemanagement.dto.DueDto;
import com.college.duemanagement.entity.Due;
import com.college.duemanagement.entity.User;
import com.college.duemanagement.repository.DueRepository;
import com.college.duemanagement.repository.UserRepository;
import com.college.duemanagement.security.services.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/dues")
@Tag(name = "Due Management", description = "APIs for managing student dues")
public class DueController {

    @Autowired
    private DueRepository dueRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DEPARTMENT_ADMIN') or hasRole('HOD') or hasRole('PRINCIPAL') or hasRole('STUDENT')")
    @Operation(summary = "Get all dues based on user role")
    public ResponseEntity<List<DueDto>> getAllDues() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Due> dues;

        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            dues = dueRepository.findAll(); // Admin sees all dues
        } else if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_HOD"))
                || userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PRINCIPAL"))) {
            String department = userDetails.getDepartment();

            List<User> studentsInDept = userRepository.findByDepartment(department);
            dues = studentsInDept.stream()
                    .flatMap(student -> dueRepository.findByStudent(student).stream().filter(due -> due.getPaymentStatus() == Due.PaymentStatus.APPROVED))
                    .collect(Collectors.toList());
        }else if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"))) {
            Optional<User> studentOptional = userRepository.findById(userDetails.getId());
            dues = studentOptional.map(dueRepository::findByStudent).orElse(List.of()); // Student sees own dues
        } else {
            // Department admin sees dues for their department
            dues = dueRepository.findByDepartment(userDetails.getDepartment());
        }

        List<DueDto> dueDtos = dues.stream().map(this::convertToDto).collect(Collectors.toList());
        return ResponseEntity.ok(dueDtos);
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DEPARTMENT_ADMIN') or hasRole('HOD') or hasRole('PRINCIPAL') or @userSecurity.isCurrentUser(#studentId)")
    @Operation(summary = "Get all dues for a specific student")
    public ResponseEntity<List<DueDto>> getDuesByStudent(@PathVariable Long studentId) {
        Optional<User> studentOptional = userRepository.findById(studentId);
        if (studentOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Due> dues;

        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            dues = dueRepository.findByStudent(studentOptional.get()); // Admin sees all student dues
        } else if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PRINCIPAL") ||
                a.getAuthority().equals("ROLE_HOD"))) {
            dues = dueRepository.findByStudentAndDepartment(studentOptional.get(), userDetails.getDepartment()); // HOD/Principal sees department-specific dues
        } else if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_DEPARTMENT_ADMIN"))) {
            dues = dueRepository.findByStudentAndDepartment(studentOptional.get(), userDetails.getDepartment()); // Dept admin sees department-specific dues
        } else {
            dues = dueRepository.findByStudent(studentOptional.get()); // Student sees own dues
        }

        List<DueDto> dueDtos = dues.stream().map(this::convertToDto).collect(Collectors.toList());
        return ResponseEntity.ok(dueDtos);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DEPARTMENT_ADMIN') or hasRole('HOD') or hasRole('PRINCIPAL') or @userSecurity.canAccessDue(#id)")
    @Operation(summary = "Get a specific due by ID")
    public ResponseEntity<DueDto> getDueById(@PathVariable Long id) {
        Optional<Due> dueOptional = dueRepository.findById(id);
        return dueOptional.map(due -> ResponseEntity.ok(convertToDto(due))).orElseGet(() -> ResponseEntity.notFound().build());

    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DEPARTMENT_ADMIN')")
    @Operation(summary = "Create a new due")
    public ResponseEntity<?> createDue(@Valid @RequestBody CreateDueRequest createDueRequest) {
        Optional<User> studentOptional = userRepository.findById(createDueRequest.getStudentId());
        if (studentOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("Error: Student not found!");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Department admin can create dues for any student, but the due is tied to their department
        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_DEPARTMENT_ADMIN"))) {
            createDueRequest.setDepartment(userDetails.getDepartment()); // Force due to admin's department
        }

        Due due = new Due();
        due.setStudent(studentOptional.get());
        due.setDepartment(createDueRequest.getDepartment());
        due.setDescription(createDueRequest.getDescription());
        due.setAmount(createDueRequest.getAmount());
        due.setDueDate(createDueRequest.getDueDate());
        due.setPaymentStatus(Due.PaymentStatus.PENDING);
        due.setCreatedBy(userDetails.getId());
        due.setCreatedAt(LocalDateTime.now());

        dueRepository.save(due);

        return ResponseEntity.ok(convertToDto(due));
    }

    @PutMapping("/{id}/pay")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Pay a due")
    public ResponseEntity<?> payDue(@PathVariable Long id, @RequestParam String paymentReference) {
        if (paymentReference == null || paymentReference.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Error: Payment reference cannot be empty!");
        }

        Optional<Due> dueOptional = dueRepository.findById(id);
        if (dueOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Due due = dueOptional.get();
        due.setPaymentStatus(Due.PaymentStatus.PAID);
        due.setPaymentDate(LocalDateTime.now());
        due.setPaymentReference(paymentReference);
        due.setUpdatedAt(LocalDateTime.now());

        dueRepository.save(due);

        return ResponseEntity.ok(convertToDto(due));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DEPARTMENT_ADMIN')")
    @Operation(summary = "Approve a paid due")
    public ResponseEntity<?> approveDue(@PathVariable Long id) {
        Optional<Due> dueOptional = dueRepository.findById(id);
        if (dueOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Due due = dueOptional.get();
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Department admin can only approve dues for their department
        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_DEPARTMENT_ADMIN"))) {
            if (!due.getDepartment().equals(userDetails.getDepartment())) {
                return ResponseEntity.badRequest().body("Error: You can only approve dues for your department!");
            }
        }

        if (due.getPaymentStatus() != Due.PaymentStatus.PAID) {
            return ResponseEntity.badRequest().body("Error: Due must be paid before approval!");
        }

        due.setPaymentStatus(Due.PaymentStatus.APPROVED);
        due.setApprovedBy(userDetails.getId());
        due.setApprovalDate(LocalDateTime.now());
        due.setUpdatedAt(LocalDateTime.now());

        dueRepository.save(due);

        return ResponseEntity.ok(convertToDto(due));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DEPARTMENT_ADMIN')")
    @Operation(summary = "Reject a paid due")
    public ResponseEntity<?> rejectDue(@PathVariable Long id) {
        Optional<Due> dueOptional = dueRepository.findById(id);
        if (dueOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Due due = dueOptional.get();
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Department admin can only reject dues for their department
        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_DEPARTMENT_ADMIN"))) {
            if (!due.getDepartment().equals(userDetails.getDepartment())) {
                return ResponseEntity.badRequest().body("Error: You can only reject dues for your department!");
            }
        }

        due.setPaymentStatus(Due.PaymentStatus.REJECTED);
        due.setUpdatedAt(LocalDateTime.now());

        dueRepository.save(due);

        return ResponseEntity.ok(convertToDto(due));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DEPARTMENT_ADMIN')")
    @Operation(summary = "Delete a due")
    public ResponseEntity<?> deleteDue(@PathVariable Long id) {
        Optional<Due> dueOptional = dueRepository.findById(id);
        if (dueOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Due due = dueOptional.get();
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Department admin can only delete dues for their department
        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_DEPARTMENT_ADMIN"))) {
            if (!due.getDepartment().equals(userDetails.getDepartment())) {
                return ResponseEntity.badRequest().body("Error: You can only delete dues for your department!");
            }
            if (!due.getStudent().getDepartment().equals(userDetails.getDepartment())) {
                return ResponseEntity.badRequest().body("Error: You can only delete dues for students in your department!");
            }
            if (due.getPaymentStatus() != Due.PaymentStatus.PENDING) {
                return ResponseEntity.badRequest().body("Error: You can only delete pending dues!");
            }
        }

        dueRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/generate-receipt")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DEPARTMENT_ADMIN')")
    @Operation(summary = "Generate a receipt for an approved due")
    public ResponseEntity<?> generateReceipt(@PathVariable Long id) {
        Optional<Due> dueOptional = dueRepository.findById(id);
        if (dueOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Due due = dueOptional.get();
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Department admin can only generate receipts for their department
        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_DEPARTMENT_ADMIN"))) {
            if (!due.getDepartment().equals(userDetails.getDepartment())) {
                return ResponseEntity.badRequest().body("Error: You can only generate receipts for your department!");
            }
        }

        // Check payment status
        if (due.getPaymentStatus() != Due.PaymentStatus.APPROVED) {
            return ResponseEntity.badRequest().body("Error: Due must be approved before generating a receipt!");
        }

        // If receipt already generated, return the due details with existing receipt number
        if (due.isReceiptGenerated()) {
            return ResponseEntity.ok(convertToDto(due));
        }

        // Generate a unique receipt number (e.g., RCPT-<id>-<yyyyMMddHHmmss>)
        String receiptNumber = "IPS-"+userDetails.getDepartment()+"-" + due.getId() + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        due.setReceiptNumber(receiptNumber);
        due.setReceiptGenerated(true);
        due.setUpdatedAt(LocalDateTime.now());
        dueRepository.save(due);

        return ResponseEntity.ok(convertToDto(due));
    }
    private DueDto convertToDto(Due due) {
        DueDto dueDto = new DueDto();
        dueDto.setId(due.getId());
        User student = due.getStudent();
        if (student != null) {
            dueDto.setStudentId(student.getId());
            dueDto.setRollNumber(student.getStudent().getRollNumber());
            dueDto.setStudentName(student.getFirstName() + " " + student.getLastName());
        } else {
            dueDto.setStudentName("Unknown Student");
        }
        dueDto.setDepartment(due.getDepartment());
        dueDto.setDescription(due.getDescription());
        dueDto.setAmount(due.getAmount());
        dueDto.setDueDate(due.getDueDate());
        dueDto.setPaymentStatus(due.getPaymentStatus().name());
        dueDto.setPaymentDate(due.getPaymentDate());
        dueDto.setPaymentReference(due.getPaymentReference());
        dueDto.setApprovedBy(due.getApprovedBy());
        dueDto.setReceiptGenerated(due.isReceiptGenerated());
        dueDto.setReceiptNumber(due.getReceiptNumber()); // Include receipt number

        if (due.getApprovedBy() != null) {
            Optional<User> approverOptional = userRepository.findById(due.getApprovedBy());
            approverOptional.ifPresent(approve ->
                    dueDto.setApprovedByName(approve.getFirstName() + " " + approve.getLastName()));
        }

        dueDto.setApprovalDate(due.getApprovalDate());
        dueDto.setCreatedAt(due.getCreatedAt());
        dueDto.setUpdatedAt(due.getUpdatedAt());

        return dueDto;
    }
}