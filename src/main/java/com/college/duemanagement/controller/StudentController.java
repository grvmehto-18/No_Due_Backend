package com.college.duemanagement.controller;

import com.college.duemanagement.entity.Student;
import com.college.duemanagement.service.StudentService;
import com.college.duemanagement.payload.request.CreateStudentRequest;
import com.college.duemanagement.payload.request.UpdateStudentRequest;
import com.college.duemanagement.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HOD', 'DEPARTMENT_ADMIN','PRINCIPAL')")
    public ResponseEntity<List<Student>> getAllStudents() {
        System.out.println(studentService.getAllStudents());
        return ResponseEntity.ok(studentService.getAllStudents());
    }

    @GetMapping("/department/{department}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOD', 'DEPARTMENT_ADMIN')")
    public ResponseEntity<List<Student>> getStudentsByDepartment(@PathVariable String department) {
        return ResponseEntity.ok(studentService.getStudentsByDepartment(department));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOD', 'DEPARTMENT_ADMIN')")
    public ResponseEntity<Student> getStudentById(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.getStudentById(id));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOD', 'DEPARTMENT_ADMIN', 'STUDENT')")
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
    public ResponseEntity<Student> createStudent(@Valid @RequestBody CreateStudentRequest request) {
        return ResponseEntity.ok(studentService.createStudent(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOD')")
    public ResponseEntity<Student> updateStudent(@PathVariable Long id, @Valid @RequestBody UpdateStudentRequest request) {
        return ResponseEntity.ok(studentService.updateStudent(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.ok().build();
    }
} 