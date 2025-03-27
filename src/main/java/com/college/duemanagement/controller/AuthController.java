package com.college.duemanagement.controller;

import com.college.duemanagement.dto.JwtResponse;
import com.college.duemanagement.dto.LoginRequest;
import com.college.duemanagement.dto.MessageResponse;
import com.college.duemanagement.repository.RoleRepository;
import com.college.duemanagement.repository.UserRepository;
import com.college.duemanagement.security.jwt.JwtUtils;
import com.college.duemanagement.security.services.UserDetailsImpl;
import com.college.duemanagement.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;
    
    @Autowired
    private UserDetailsService userDetailsService;

    public AuthController(
            UserRepository userRepository,
            RoleRepository roleRepository,
            EmailService emailService) {
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        logger.debug("Login attempt for username: {}", loginRequest.getUsername());
        
        try {
            Authentication authentication = this.authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);
            
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            logger.debug("Login successful for user: {}", userDetails.getUsername());
            
            return ResponseEntity.ok(new JwtResponse(jwt,
                                                   userDetails.getId(),
                                                   userDetails.getUsername(),
                                                   userDetails.getEmail(),
                                                   roles,
                                                   userDetails.getDepartment()));
        } catch (UsernameNotFoundException e) {
            logger.error("Authentication failed for username: {}, error: {}", 
                loginRequest.getUsername(), e.getMessage());
            throw e;
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        // Implementation for password reset functionality
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
        // Implementation for password reset functionality
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        logger.debug("Token refresh attempt");
        
        try {
            // Extract the token from the Authorization header
            String headerAuth = request.getHeader("Authorization");
            String jwt = null;
            
            if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
                jwt = headerAuth.substring(7);
            } else {
                // Try to get token from cookie
                Cookie[] cookies = request.getCookies();
                if (cookies != null) {
                    for (Cookie cookie : cookies) {
                        if ("jwt".equals(cookie.getName())) {
                            jwt = cookie.getValue();
                            break;
                        }
                    }
                }
            }
            
            if (jwt == null) {
                logger.error("No token found in request");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("No token found"));
            }
            
            // Validate the token
            if (!jwtUtils.validateJwtToken(jwt)) {
                logger.error("Invalid JWT token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Invalid JWT token"));
            }
            
            // Get username from the token
            String username = jwtUtils.getUserNameFromJwtToken(jwt);
            
            // Load user details
            UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(username);
            
            // Create authentication object
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            
            // Generate new token
            String newToken = jwtUtils.generateJwtToken(authentication);
            
            // Get user roles
            List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
            
            logger.debug("Token refreshed successfully for user: {}", username);
            
            // Return new token
            return ResponseEntity.ok(new JwtResponse(
                newToken,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles,
                userDetails.getDepartment()));
                
        } catch (Exception e) {
            logger.error("Error refreshing token: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new MessageResponse("Error refreshing token"));
        }
    }
} 