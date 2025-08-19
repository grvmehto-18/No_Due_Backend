package com.college.duemanagement.repository;

import com.college.duemanagement.entity.Due;
import com.college.duemanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DueRepository extends JpaRepository<Due, Long> {
    
    List<Due> findByStudent(User student);
    
    List<Due> findByStudentAndDepartment(User student, String department);
    
    List<Due> findByDepartment(String department);
    
    List<Due> findByStudentAndPaymentStatus(User student, Due.PaymentStatus paymentStatus);
    
    List<Due> findByDepartmentAndPaymentStatus(String department, Due.PaymentStatus paymentStatus);
    
    List<Due> findByStudentAndDepartmentAndPaymentStatus(User student, String department, Due.PaymentStatus paymentStatus);
    
    long countByPaymentStatus(Due.PaymentStatus status);
    
    long countByDepartmentAndPaymentStatus(String department, Due.PaymentStatus status);
    
    List<Due> findTop5ByPaymentStatusOrderByUpdatedAtDesc(Due.PaymentStatus status);

    @Query("SELECT d FROM Due d " +
            "WHERE d.student.department = :department " +
            "AND EXISTS (SELECT r FROM d.student.roles r WHERE r.name = 'ROLE_STUDENT') " +
            "AND NOT EXISTS (SELECT d2 FROM Due d2 WHERE d2.student = d.student AND d2.paymentStatus != 'APPROVED')")
    List<Due> findClearedDuesByDepartment(@Param("department") String department);

    // New method to find students with no pending dues across all departments
    @Query("SELECT DISTINCT d.student FROM Due d WHERE d.student NOT IN " +
            "(SELECT d2.student FROM Due d2 WHERE d2.paymentStatus = 'PENDING')")
    List<User> findStudentsWithNoPendingDues();

    // Helper method to get dues for a specific student
    List<Due> findByStudentAndPaymentStatusNot(User student, Due.PaymentStatus status);
} 