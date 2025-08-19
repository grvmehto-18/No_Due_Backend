package com.college.duemanagement.service;

import com.college.duemanagement.dto.DueDto;
import com.college.duemanagement.entity.Due;

import java.util.List;

public interface DueService {
    DueDto createDue(DueDto dueDto);
    DueDto getDue(Long id);
    List<DueDto> getAllDues();
    List<DueDto> getDuesByDepartment(String department);
    List<DueDto> getDuesByStudent(String studentId);
    DueDto updateDue(Long id, DueDto dueDto);
    DueDto approveDue(Long id, Long approvedBy);
    void deleteDue(Long id);
    
    // Method to check if a student has cleared all dues
    boolean hasStudentClearedAllDues(String studentId);
    
    // Method to check if a student has pending dues in a specific department
    boolean hasStudentPendingDuesInDepartment(Long studentId, String department);
} 