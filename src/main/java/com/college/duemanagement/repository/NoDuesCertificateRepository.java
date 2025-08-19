package com.college.duemanagement.repository;

import com.college.duemanagement.entity.NoDuesCertificate;
import com.college.duemanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoDuesCertificateRepository extends JpaRepository<NoDuesCertificate, Long> {
    List<NoDuesCertificate> findByStudent(User student);
    Optional<NoDuesCertificate> findByCertificateNumber(String certificateNumber);
    List<NoDuesCertificate> findByStatus(NoDuesCertificate.CertificateStatus status);
    List<NoDuesCertificate> findByPrincipalSigned(Boolean principalSigned);
    List<NoDuesCertificate> findByStudentDepartment(String department);

} 