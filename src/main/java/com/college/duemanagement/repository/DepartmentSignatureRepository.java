package com.college.duemanagement.repository;

import com.college.duemanagement.entity.DepartmentSignature;
import com.college.duemanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartmentSignatureRepository extends JpaRepository<DepartmentSignature, Long> {
    List<DepartmentSignature> findByStudent(User student);
    List<DepartmentSignature> findByStudentAndDepartment(User student, String department);
    List<DepartmentSignature> findByDepartment(String department);
    List<DepartmentSignature> findByStatus(DepartmentSignature.SignatureStatus status);
    List<DepartmentSignature> findByDepartmentAndStatus(String department, DepartmentSignature.SignatureStatus status);
} 