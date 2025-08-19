package com.college.duemanagement.controller;

import com.college.duemanagement.entity.Student;
import com.college.duemanagement.service.StudentService;
import com.college.duemanagement.payload.request.CreateStudentRequest;
import com.college.duemanagement.payload.request.UpdateStudentRequest;
import com.college.duemanagement.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/students")
@Tag(name = "Student Management", description = "APIs for managing students")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HOD', 'DEPARTMENT_ADMIN','PRINCIPAL')")
    @Operation(summary = "Get all students")
    public ResponseEntity<List<Student>> getAllStudents() {
        System.out.println(studentService.getAllStudents());
        return ResponseEntity.ok(studentService.getAllStudents());
    }

    @GetMapping("/department/{department}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOD', 'DEPARTMENT_ADMIN')")
    @Operation(summary = "Get students by department")
    public ResponseEntity<List<Student>> getStudentsByDepartment(@PathVariable String department) {
        return ResponseEntity.ok(studentService.getStudentsByDepartment(department));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOD', 'DEPARTMENT_ADMIN')")
    @Operation(summary = "Get a student by ID")
    public ResponseEntity<Student> getStudentById(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.getStudentById(id));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOD', 'DEPARTMENT_ADMIN', 'STUDENT')")
    @Operation(summary = "Get a student by user ID")
    public ResponseEntity<Student> getStudentByUserId(@PathVariable Long userId) {
        try {
            Student student = studentService.getStudentByUserId(userId);
            return ResponseEntity.ok(student);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving student with user id: " + userId, e);
        }
    }

    @GetMapping("/roll/{rollNumber}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOD', 'DEPARTMENT_ADMIN')")
    @Operation(summary = "Get a student by roll number")
    public ResponseEntity<Student> getStudentByRollNumber(@PathVariable String rollNumber) {
        try {
            Student student = studentService.getStudentByRollNumber(rollNumber);
            return ResponseEntity.ok(student);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving student with roll number: " + rollNumber, e);
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HOD')")
    @Operation(summary = "Create a new student")
    public ResponseEntity<Student> createStudent(@Valid @RequestBody CreateStudentRequest request) {
        return ResponseEntity.ok(studentService.createStudent(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOD')")
    @Operation(summary = "Update an existing student")
    public ResponseEntity<Student> updateStudent(@PathVariable Long id, @Valid @RequestBody UpdateStudentRequest request) {
        return ResponseEntity.ok(studentService.updateStudent(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a student")
    public ResponseEntity<?> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.ok().build();
    }
} 