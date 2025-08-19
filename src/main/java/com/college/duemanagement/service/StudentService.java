package com.college.duemanagement.service;

import com.college.duemanagement.entity.Student;
import com.college.duemanagement.entity.User;
import com.college.duemanagement.repository.StudentRepository;
import com.college.duemanagement.repository.UserRepository;
import com.college.duemanagement.payload.request.CreateStudentRequest;
import com.college.duemanagement.payload.request.UpdateStudentRequest;
import com.college.duemanagement.exception.ResourceNotFoundException;
import com.college.duemanagement.exception.BadRequestException;
import com.college.duemanagement.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.logging.Logger;

@Service
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    public List<Student> getAllStudents() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        System.out.println("UserDetails: " + userDetails);
        System.out.println("Authorities: " + userDetails.getAuthorities());

        // If user has ROLE_ADMIN, return all students
        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            List<Student> students = studentRepository.findAll();
            System.out.println("All students (ROLE_ADMIN): " + students.size() + " students retrieved");
            if (students.isEmpty()) {
                System.out.println("No students found in the database");
            }
            return students;
        }

        // If user has ROLE_HOD or ROLE_PRINCIPAL, return students from their department
        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_HOD")) ||
                userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PRINCIPAL"))) {
            String department = userDetails.getDepartment();
            System.out.println("Fetching students for department: " + department);
            List<Student> students = studentRepository.findByUserDepartment(department);
            System.out.println("Students found (by department): " + students.size() + " students retrieved");
            if (students.isEmpty()) {
                System.out.println("No students found for department: " + department);
            }
            return students;
        }

        // Default case: return all students (or adjust based on your requirements)
        List<Student> students = studentRepository.findAll();
        System.out.println("All students (default): " + students.size() + " students retrieved");
        if (students.isEmpty()) {
            System.out.println("No students found in the database");
        }
        return students;
    }
    public List<Student> getStudentsByDepartment(String department) {
        return studentRepository.findByUserDepartment(department);
    }

    public Student getStudentById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
    }

    public Student getStudentByUserId(Long userId) {
        return studentRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with user id: " + userId));
    }

    public Student getStudentByRollNumber(String rollNumber) {
        return studentRepository.findByRollNumber(rollNumber)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found with roll number: " + rollNumber));
    }

    @Transactional
    public Student createStudent(CreateStudentRequest request) {
        // Check if roll number is already in use
        if (studentRepository.existsByRollNumber(request.getRollNumber())) {
            throw new BadRequestException("Roll number is already in use");
        }

        // Create user first
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_STUDENT");
        
        User user = userService.createUser(
            request.getEmail(),
            request.getFirstName(),
            request.getLastName(),
            request.getDepartment(),
            roles
        );

        // Create student and set the user
        Student student = new Student();
        student.setUser(user); // This sets the user_id
        student.setRollNumber(request.getRollNumber());
        student.setSemester(request.getSemester());
        student.setBatch(request.getBatch());
        student.setCourse(request.getCourse());
        student.setSection(request.getSection());
        student.setFatherName(request.getFatherName());
        student.setMotherName(request.getMotherName());
        student.setContactNumber(request.getContactNumber());
        student.setAddress(request.getAddress());

        // Save and flush to ensure the user is saved before the student
        studentRepository.saveAndFlush(student);

        return student;
    }

    @Transactional
    public Student updateStudent(Long id, UpdateStudentRequest request) {
        // Find the student by id
        Student student = getStudentById(id);
        
        // Check if roll number is already in use by another student
        if (!student.getRollNumber().equals(request.getRollNumber()) && 
            studentRepository.existsByRollNumber(request.getRollNumber())) {
            throw new BadRequestException("Roll number is already in use");
        }
        
        // Update user information
        User user = student.getUser();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setDepartment(request.getDepartment());
        user.setEmail(request.getEmail());
        userRepository.save(user);
        
        // Update student information
        student.setRollNumber(request.getRollNumber());
        student.setSemester(request.getSemester());
        student.setBatch(request.getBatch());
        student.setCourse(request.getCourse());
        student.setSection(request.getSection());
        student.setFatherName(request.getFatherName());
        student.setMotherName(request.getMotherName());
        student.setContactNumber(request.getContactNumber());
        student.setAddress(request.getAddress());
        
        return studentRepository.save(student);
    }

    @Transactional
    public void deleteStudent(Long id) {
        // Fetch the student
        Student student = getStudentById(id);
        if (student == null) {
            throw new RuntimeException("Student not found with id: " + id);
        }

        User user = student.getUser();
        if (user != null) {
            // Deleting the User will cascade to Student and DepartmentSignature
            userRepository.delete(user);
        } else {
            // If no User is associated, just delete the Student
            studentRepository.delete(student);
        }
    }

} 