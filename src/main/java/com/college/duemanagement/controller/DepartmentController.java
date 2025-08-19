package com.college.duemanagement.controller;

import com.college.duemanagement.dto.CreateDepartmentRequest;
import com.college.duemanagement.dto.DepartmentDto;
import com.college.duemanagement.entity.Department;
import com.college.duemanagement.entity.User;
import com.college.duemanagement.repository.DepartmentRepository;
import com.college.duemanagement.repository.UserRepository;
import com.college.duemanagement.enums.DepartmentEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/departments")
@Tag(name = "Department Management", description = "APIs for managing departments")
public class DepartmentController {
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    public DepartmentController(
            DepartmentRepository departmentRepository,
            UserRepository userRepository) {
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    @Operation(summary = "Get all departments")
    public ResponseEntity<List<DepartmentDto>> getAllDepartments() {
        List<Department> departments = departmentRepository.findAll();
        List<DepartmentDto> departmentDtos = departments.stream().map(this::convertToDto).collect(Collectors.toList());
        return ResponseEntity.ok(departmentDtos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a department by ID")
    public ResponseEntity<DepartmentDto> getDepartmentById(@PathVariable Long id) {
        Optional<Department> departmentOptional = departmentRepository.findById(id);
        if (!departmentOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(convertToDto(departmentOptional.get()));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new department")
    public ResponseEntity<?> createDepartment(@Valid @RequestBody CreateDepartmentRequest createDepartmentRequest) {
        if (departmentRepository.existsByName(createDepartmentRequest.getName())) {
            return ResponseEntity.badRequest().body("Error: Department name is already taken!");
        }

        if (departmentRepository.existsByCode(createDepartmentRequest.getCode())) {
            return ResponseEntity.badRequest().body("Error: Department code is already in use!");
        }

        Department department = new Department();
        department.setName(createDepartmentRequest.getName());
        department.setCode(createDepartmentRequest.getCode());
        department.setDescription(createDepartmentRequest.getDescription());
        department.setCreatedAt(LocalDateTime.now());

        // Set HOD if provided
        if (createDepartmentRequest.getHodId() != null) {
            Optional<User> hodOptional = userRepository.findById(createDepartmentRequest.getHodId());
            if (hodOptional.isPresent()) {
                department.setHod(hodOptional.get());
            }
        }

        // Set Admin if provided
        if (createDepartmentRequest.getAdminId() != null) {
            Optional<User> adminOptional = userRepository.findById(createDepartmentRequest.getAdminId());
            if (adminOptional.isPresent()) {
                department.setAdmin(adminOptional.get());
            }
        }

        departmentRepository.save(department);

        return ResponseEntity.ok(convertToDto(department));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing department")
    public ResponseEntity<?> updateDepartment(@PathVariable Long id, @Valid @RequestBody DepartmentDto departmentDto) {
        Optional<Department> departmentOptional = departmentRepository.findById(id);
        if (!departmentOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Department department = departmentOptional.get();

        // Check if name is being changed and if it's already taken
        if (!department.getName().equals(departmentDto.getName()) &&
            departmentRepository.existsByName(departmentDto.getName())) {
            return ResponseEntity.badRequest().body("Error: Department name is already taken!");
        }

        // Check if code is being changed and if it's already in use
        if (!department.getCode().equals(departmentDto.getCode()) &&
            departmentRepository.existsByCode(departmentDto.getCode())) {
            return ResponseEntity.badRequest().body("Error: Department code is already in use!");
        }

        department.setName(departmentDto.getName());
        department.setCode(departmentDto.getCode());
        department.setDescription(departmentDto.getDescription());
        department.setUpdatedAt(LocalDateTime.now());

        // Update HOD if provided
        if (departmentDto.getHodId() != null) {
            Optional<User> hodOptional = userRepository.findById(departmentDto.getHodId());
            if (hodOptional.isPresent()) {
                department.setHod(hodOptional.get());
            }
        } else {
            department.setHod(null);
        }

        // Update Admin if provided
        if (departmentDto.getAdminId() != null) {
            Optional<User> adminOptional = userRepository.findById(departmentDto.getAdminId());
            if (adminOptional.isPresent()) {
                department.setAdmin(adminOptional.get());
            }
        } else {
            department.setAdmin(null);
        }

        departmentRepository.save(department);

        return ResponseEntity.ok(convertToDto(department));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a department")
    public ResponseEntity<?> deleteDepartment(@PathVariable Long id) {
        Optional<Department> departmentOptional = departmentRepository.findById(id);
        if (!departmentOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        departmentRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/list")
    @Operation(summary = "Get a list of all department enums")
    public ResponseEntity<List<Map<String, String>>> getDepartmentList() {
        List<Map<String, String>> departments = Arrays.stream(DepartmentEnum.values())
            .map(dept -> Map.of(
                "name", dept.name(),
                "displayName", dept.getDisplayName()
            ))
            .collect(Collectors.toList());
        return ResponseEntity.ok(departments);
    }

    private DepartmentDto convertToDto(Department department) {
        DepartmentDto departmentDto = new DepartmentDto();
        departmentDto.setId(department.getId());
        departmentDto.setName(department.getName());
        departmentDto.setCode(department.getCode());
        departmentDto.setDescription(department.getDescription());

        if (department.getHod() != null) {
            departmentDto.setHodId(department.getHod().getId());
            departmentDto.setHodName(department.getHod().getFirstName() + " " + department.getHod().getLastName());
        }

        if (department.getAdmin() != null) {
            departmentDto.setAdminId(department.getAdmin().getId());
            departmentDto.setAdminName(department.getAdmin().getFirstName() + " " + department.getAdmin().getLastName());
        }

        departmentDto.setCreatedAt(department.getCreatedAt());
        departmentDto.setUpdatedAt(department.getUpdatedAt());

        return departmentDto;
    }
}