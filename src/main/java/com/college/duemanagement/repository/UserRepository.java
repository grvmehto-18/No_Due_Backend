package com.college.duemanagement.repository;

import com.college.duemanagement.entity.Role;
import com.college.duemanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByUniqueCode(String uniqueCode);
    
    Optional<User> findByResetToken(String resetToken);
    
    Boolean existsByUsername(String username);
    
    Boolean existsByEmail(String email);
    
    Boolean existsByUniqueCode(String uniqueCode);

    List<User> findByDepartment(String department);

    @Query("SELECT u FROM User u WHERE NOT EXISTS (" +
            "SELECT r FROM u.roles r WHERE r.name = 'ROLE_STUDENT')")
    List<User> notExistsRoleStudent();

    long countByDepartment(String department);

    Optional<User> findByDepartmentAndRoles_Name(String department, Role.ERole roleName);
} 