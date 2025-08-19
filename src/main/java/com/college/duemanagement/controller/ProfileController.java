package com.college.duemanagement.controller;

import com.college.duemanagement.entity.User;
import com.college.duemanagement.service.UserService;
import com.college.duemanagement.payload.request.ProfileUpdateRequest;
import com.college.duemanagement.payload.response.JwtResponse;
import com.college.duemanagement.security.jwt.JwtUtils;
import com.college.duemanagement.security.services.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatus;
import java.io.IOException;
import java.util.Base64;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Profile Management", description = "APIs for managing user profiles")
public class ProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @PutMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update user profile")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody ProfileUpdateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        if (userDetails == null) {
            throw new AccessDeniedException("Not authenticated");
        }
        
        User updatedUser = userService.updateProfile(userDetails.getId(), request);

        // If password was changed, generate new token
        if (request.getNewPassword() != null && !request.getNewPassword().isEmpty()) {
            // Authenticate with new password to get new token
            Authentication newAuth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(updatedUser.getUsername(), request.getNewPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(newAuth);
            String jwt = jwtUtils.generateJwtToken(newAuth);

            return ResponseEntity.ok(new JwtResponse(
                jwt,
                updatedUser.getId(),
                updatedUser.getUsername(),
                updatedUser.getEmail(),
                updatedUser.getFirstName(),
                updatedUser.getLastName(),
                updatedUser.getRoles(),
                updatedUser.getDepartment(),
                updatedUser.getUniqueCode()
            ));
        }

        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get user profile")
    public ResponseEntity<User> getProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        if (userDetails == null) {
            throw new AccessDeniedException("Not authenticated");
        }
        
        User user = userService.getUserById(userDetails.getId());
        return ResponseEntity.ok(user);
    }
    
    @PostMapping("/signature")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Upload user signature")
    public ResponseEntity<?> uploadSignature(@RequestParam("file") MultipartFile file) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            
            if (userDetails == null) {
                throw new AccessDeniedException("Not authenticated");
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
            userService.saveSignature(userDetails.getId(), file.getBytes());
            
            return ResponseEntity.ok().body("Signature uploaded successfully");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to upload signature: " + e.getMessage());
        }
    }
    
    @GetMapping("/signature")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get user signature")
    public ResponseEntity<?> getSignature() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        if (userDetails == null) {
            throw new AccessDeniedException("Not authenticated");
        }
        
        User user = userService.getUserById(userDetails.getId());
        
        if (user.getESignature() == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Convert byte array to Base64 string
        String base64Signature = Base64.getEncoder().encodeToString(user.getESignature());
        
        return ResponseEntity.ok()
            .body(base64Signature);
    }
} 