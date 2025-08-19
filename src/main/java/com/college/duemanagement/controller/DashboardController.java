package com.college.duemanagement.controller;

import com.college.duemanagement.dto.DashboardStatsDto;
import com.college.duemanagement.dto.DepartmentStatDto;
import com.college.duemanagement.dto.ActivityDto;
import com.college.duemanagement.entity.Due;
import com.college.duemanagement.entity.Student;
import com.college.duemanagement.entity.User;
import com.college.duemanagement.repository.*;
import com.college.duemanagement.security.services.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "APIs for dashboard statistics and activities")
public class DashboardController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private DueRepository dueRepository;

    @Autowired
    private DepartmentRepository departmentRepository;


    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HOD') or hasRole('DEPARTMENT_ADMIN') or hasRole('PRINCIPAL') or hasRole('STUDENT')")
    @Operation(summary = "Get dashboard statistics")
    public ResponseEntity<DashboardStatsDto> getDashboardStats() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        DashboardStatsDto stats = new DashboardStatsDto();

        // Get total counts based on user role and department
        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") ||
                                                               a.getAuthority().equals("ROLE_PRINCIPAL"))) {
            // Admin and Principal can see all stats
            stats.setTotalStudents(studentRepository.count());
            stats.setTotalUsers(userRepository.count());
            stats.setTotalDepartments(departmentRepository.count());
            stats.setTotalDues(dueRepository.countByPaymentStatus(Due.PaymentStatus.PENDING));
        } else if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"))) {
            // Students can see only their department stats
            String department = userDetails.getDepartment();
            stats.setTotalStudents(studentRepository.countByUserDepartment(department));
            stats.setTotalUsers(0L); // Students don't need to see user counts
            stats.setTotalDepartments(1L); // Only their department
            stats.setTotalDues(dueRepository.countByDepartmentAndPaymentStatus(department, Due.PaymentStatus.PENDING));
        } else {
            // Department Admin and HOD can see only their department stats
            String department = userDetails.getDepartment();
            stats.setTotalStudents(studentRepository.count());
            stats.setTotalUsers(userRepository.countByDepartment(department));
            stats.setTotalDepartments(1L); // Only their department
            stats.setTotalDues(dueRepository.countByDepartmentAndPaymentStatus(department, Due.PaymentStatus.PENDING));
        }

        // Get department stats based on user role
        List<DepartmentStatDto> departmentStats;
        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") ||
                                                               a.getAuthority().equals("ROLE_PRINCIPAL"))) {
            // Admin and Principal can see all departments
            departmentStats = departmentRepository.findAll().stream()
                .map(dept -> {
                    DepartmentStatDto statDto = new DepartmentStatDto();
                    statDto.setDepartment(dept.getName());

                    long studentCount = studentRepository.countByUserDepartment(dept.getName());
                    statDto.setStudentCount(studentCount);

                    List<Due> departmentDues = dueRepository.findByDepartment(dept.getName());
                    double totalDueAmount = departmentDues.stream()
                        .filter(due -> due.getPaymentStatus() == Due.PaymentStatus.PENDING)
                        .mapToDouble(due -> due.getAmount().doubleValue())
                        .sum();
                    statDto.setDueAmount(totalDueAmount);

                    long totalDues = departmentDues.size();
                    long completedDues = departmentDues.stream()
                        .filter(due -> due.getPaymentStatus() == Due.PaymentStatus.PAID ||
                                     due.getPaymentStatus() == Due.PaymentStatus.APPROVED)
                        .count();
                    double completionRate = totalDues > 0 ? (completedDues * 100.0) / totalDues : 100;
                    statDto.setCompletionRate((int) completionRate);

                    return statDto;
                })
                .collect(Collectors.toList());
        } else {
            // Department Admin, HOD and Students can see only their department
            String department = userDetails.getDepartment();
            departmentStats = Collections.singletonList(
                departmentRepository.findByName(department)
                    .map(dept -> {
                        DepartmentStatDto statDto = new DepartmentStatDto();
                        statDto.setDepartment(dept.getName());

                        long studentCount = studentRepository.countByUserDepartment(dept.getName());
                        statDto.setStudentCount(studentCount);

                        List<Due> departmentDues = dueRepository.findByDepartment(dept.getName());
                        double totalDueAmount = departmentDues.stream()
                            .filter(due -> due.getPaymentStatus() == Due.PaymentStatus.PENDING)
                            .mapToDouble(due -> due.getAmount().doubleValue())
                            .sum();
                        statDto.setDueAmount(totalDueAmount);

                        long totalDues = departmentDues.size();
                        long completedDues = departmentDues.stream()
                            .filter(due -> due.getPaymentStatus() == Due.PaymentStatus.PAID ||
                                         due.getPaymentStatus() == Due.PaymentStatus.APPROVED)
                            .count();
                        double completionRate = totalDues > 0 ? (completedDues * 100.0) / totalDues : 100;
                        statDto.setCompletionRate((int) completionRate);

                        return statDto;
                    })
                    .orElseGet(() -> {
                        DepartmentStatDto statDto = new DepartmentStatDto();
                        statDto.setDepartment(department);
                        return statDto;
                    })
            );
        }
        stats.setDepartmentStats(departmentStats);

        // Get recent activities based on user role
        List<ActivityDto> recentActivities = new ArrayList<>();
        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") ||
                                                               a.getAuthority().equals("ROLE_PRINCIPAL"))) {
            // Admin and Principal can see all activities
            dueRepository.findTop5ByPaymentStatusOrderByUpdatedAtDesc(Due.PaymentStatus.PAID)
                .forEach(due -> {
                    ActivityDto activity = new ActivityDto();
                    activity.setId(due.getId());
                    activity.setType("payment");
                    activity.setDescription("Due " + due.getDescription() + " - " + due.getPaymentStatus());
                    activity.setTimestamp(due.getUpdatedAt().toString());
                    recentActivities.add(activity);
                });

            studentRepository.findTop5ByOrderByCreatedAtDesc()
                .forEach(student -> {
                    ActivityDto activity = new ActivityDto();
                    activity.setId(student.getId());
                    activity.setType("registration");
                    activity.setDescription("New student registered in " + student.getUser().getDepartment() + " department");
                    activity.setTimestamp(student.getCreatedAt().toString());
                    recentActivities.add(activity);
                });
        } else if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"))) {
            // Students can see only their own activities
            Long userId = userDetails.getId();

            // Find the student by user ID
            studentRepository.findByUserId(userId).ifPresent(student -> {
                // Get dues for this student
                User user = student.getUser();
                // In the STUDENT role section
                dueRepository.findByStudent(user)
                        .stream()
                        .limit(5)
                        .forEach(due -> {
                            ActivityDto activity = new ActivityDto();
                            activity.setId(Long.parseLong("1" + due.getId()));
                            activity.setType("payment");
                            activity.setDescription("Due " + due.getDescription() + " - " + due.getPaymentStatus());
                            // Handle null updatedAt
                            activity.setTimestamp(due.getUpdatedAt() != null ? due.getUpdatedAt().toString() : "Not updated yet");
                            recentActivities.add(activity);
                        });

                // Add student registration activity
                ActivityDto activity = new ActivityDto();
                activity.setId(Long.parseLong("2" + student.getId()));
                activity.setType("registration");
                activity.setDescription("You registered in " + student.getUser().getDepartment() + " department");
                activity.setTimestamp(student.getCreatedAt().toString());
                recentActivities.add(activity);
            });
        } else {
            // Department Admin and HOD can see only their department activities
            String department = userDetails.getDepartment();
            dueRepository.findByDepartmentAndPaymentStatus(department, Due.PaymentStatus.PAID)
                .stream()
                .limit(5)
                .forEach(due -> {
                    ActivityDto activity = new ActivityDto();
                    activity.setId(due.getId());
                    activity.setType("payment");
                    activity.setDescription("Due " + due.getDescription() + " - " + due.getPaymentStatus());
                    activity.setTimestamp(due.getUpdatedAt().toString());
                    recentActivities.add(activity);
                });

            studentRepository.findByUserDepartment(department)
                .stream()
                .limit(5)
                .forEach(student -> {
                    ActivityDto activity = new ActivityDto();
                    activity.setId(student.getId());
                    activity.setType("registration");
                    activity.setDescription("New student registered: " + student.getUser().getFirstName());
                    activity.setTimestamp(student.getCreatedAt().toString());
                    recentActivities.add(activity);
                });
        }

        // Sort activities by timestamp
        recentActivities.sort((a1, a2) -> a2.getTimestamp().compareTo(a1.getTimestamp()));
        stats.setRecentActivities(recentActivities.subList(0, Math.min(5, recentActivities.size())));

        return ResponseEntity.ok(stats);
    }
}