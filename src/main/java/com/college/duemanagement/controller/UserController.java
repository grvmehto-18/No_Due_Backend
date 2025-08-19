package com.college.duemanagement.controller;

import com.college.duemanagement.entity.User;
import com.college.duemanagement.service.UserService;
import com.college.duemanagement.payload.request.CreateUserRequest;
import com.college.duemanagement.payload.request.UpdateUserRequest;
import com.college.duemanagement.payload.response.JwtResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import com.college.duemanagement.security.services.UserDetailsImpl;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Base64;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "User Management", description = "APIs for managing users")
public class UserController {


    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('HOD')")
    @Operation(summary = "Get all users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HOD')")
    @Operation(summary = "Get a user by ID")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new user")
    public ResponseEntity<User> createUser(@Valid @RequestBody CreateUserRequest request ) {

        JwtResponse response = userService.createUser(request);
        User user = userService.getUserById(response.id());
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing user")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        User user = userService.updateUser(id, request);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a user")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/signature")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HOD') or @userSecurity.isCurrentUser(#id)")
    @Operation(summary = "Upload a signature for a user")
    public ResponseEntity<?> uploadSignature(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            // Get current user details
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            // Check if user is authorized
            if (!userDetails.getId().equals(id) &&
                !userDetails.getAuthorities().stream().anyMatch(a ->
                    a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_HOD"))) {
                throw new AccessDeniedException("You can only upload signature for your own account");
            }

            // Check file type
            if (!file.getContentType().startsWith("image/")) {
                return ResponseEntity.badRequest().body("Only image files are allowed");
            }

            // Check file size (max 1MB)
            if (file.getSize() > 1048576) {
                return ResponseEntity.badRequest().body("File size should be less than 1MB");
            }

            // Save signature
            userService.saveSignature(id, file.getBytes());

            return ResponseEntity.ok().body("Signature uploaded successfully");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to upload signature: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/signature")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HOD') or @userSecurity.isCurrentUser(#id)")
    @Operation(summary = "Get a user's signature")
    public ResponseEntity<?> getSignature(@PathVariable Long id) {
        User user = userService.getUserById(id);

        if (user.getESignature() == null) {
            return ResponseEntity.notFound().build();
        }

        // Convert byte array to Base64 string
        String base64Signature = Base64.getEncoder().encodeToString(user.getESignature());

        return ResponseEntity.ok()
            .body(base64Signature);
    }
}