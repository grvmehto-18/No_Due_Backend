package com.college.duemanagement.service.impl;

import com.college.duemanagement.dto.DueDto;
import com.college.duemanagement.entity.Due;
import com.college.duemanagement.entity.User;
import com.college.duemanagement.repository.DueRepository;
import com.college.duemanagement.repository.UserRepository;
import com.college.duemanagement.service.DueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DueServiceImpl implements DueService {

    private final DueRepository dueRepository;
    private final UserRepository userRepository;

    @Autowired
    public DueServiceImpl(DueRepository dueRepository, UserRepository userRepository) {
        this.dueRepository = dueRepository;
        this.userRepository = userRepository;
    }

    @Override
    public DueDto createDue(DueDto dueDto) {
        // Implementation would go here
        return null;
    }

    @Override
    public DueDto getDue(Long id) {
        // Implementation would go here
        return null;
    }

    @Override
    public List<DueDto> getAllDues() {
        // Implementation would go here
        return null;
    }

    @Override
    public List<DueDto> getDuesByDepartment(String department) {
        // Implementation would go here
        return null;
    }

    @Override
    public List<DueDto> getDuesByStudent(String studentId) {
        // Implementation would go here
        return null;
    }

    @Override
    public DueDto updateDue(Long id, DueDto dueDto) {
        // Implementation would go here
        return null;
    }

    @Override
    public DueDto approveDue(Long id, Long approvedBy) {
        // Implementation would go here
        return null;
    }

    @Override
    public void deleteDue(Long id) {
        // Implementation would go here
    }

    @Override
    public boolean hasStudentClearedAllDues(String studentId) {
        User student = userRepository.findByUsername(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        
        // Get all dues for the student
        List<Due> studentDues = dueRepository.findByStudent(student);
        
        // Check if there are any dues that are not in APPROVED status
        boolean hasNonApprovedDues = studentDues.stream()
                .anyMatch(due -> due.getPaymentStatus() != Due.PaymentStatus.APPROVED);
        
        // Return true if all dues are approved (or there are no dues)
        return !hasNonApprovedDues;
    }
    
    @Override
    public boolean hasStudentPendingDuesInDepartment(Long studentId, String department) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        
        // Get all dues for the student in the specified department
        List<Due> departmentDues = dueRepository.findByStudentAndDepartment(student, department);
        
        // Check if there are any dues that are not in APPROVED status
        boolean hasPendingDues = departmentDues.stream()
                .anyMatch(due -> due.getPaymentStatus() != Due.PaymentStatus.APPROVED);
        
        return hasPendingDues;
    }
}