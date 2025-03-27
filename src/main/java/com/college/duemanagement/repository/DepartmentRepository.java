package com.college.duemanagement.repository;

import com.college.duemanagement.entity.Department;
import com.college.duemanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    
    Optional<Department> findByName(String name);
    
    Optional<Department> findByCode(String code);
    
    List<Department> findByHod(User hod);
    
    List<Department> findByAdmin(User admin);
    
    Boolean existsByName(String name);
    
    Boolean existsByCode(String code);
} 