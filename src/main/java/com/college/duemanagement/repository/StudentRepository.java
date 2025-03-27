package com.college.duemanagement.repository;

import com.college.duemanagement.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByRollNumber(String rollNumber);
    Optional<Student> findByUserId(Long userId);
    List<Student> findByUserDepartment(String department);
    long countByUserDepartment(String department);
    List<Student> findTop5ByOrderByCreatedAtDesc();
    boolean existsByRollNumber(String rollNumber);
} 