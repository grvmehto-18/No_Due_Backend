package com.college.duemanagement.service.impl;

import com.college.duemanagement.dto.JwtResponse;
import com.college.duemanagement.dto.LoginRequest;
import com.college.duemanagement.dto.MessageResponse;
import com.college.duemanagement.security.jwt.JwtUtils;
import com.college.duemanagement.security.services.UserDetailsImpl;
import com.college.duemanagement.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;

    @Autowired
    public AuthServiceImpl(AuthenticationManager authenticationManager, JwtUtils jwtUtils, UserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public ResponseEntity<?> authenticateUser(LoginRequest loginRequest) {
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

    @Override
    public ResponseEntity<?> forgotPassword(String email) {
        // Implementation for password reset functionality

        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<?> resetPassword(String token, String newPassword) {
        // Implementation for password reset functionality
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        logger.debug("Token refresh attempt");

        try {
            // Extract JWT token from Authorization header or cookie
            String jwt = extractJwtFromRequest(request);

            if (jwt == null) {
                logger.warn("No token found in request");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new MessageResponse("No token found"));
            }

            // Try to validate token (even if expired - depending on your jwtUtils)
            if (!jwtUtils.isTokenRefreshable(jwt)) {  // Assuming you add this method
                logger.warn("Token not eligible for refresh");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new MessageResponse("Invalid or expired JWT token"));
            }

            String username = jwtUtils.getUserNameFromJwtToken(jwt);
            if (username == null) {
                logger.warn("Username not found in token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new MessageResponse("Invalid token payload"));
            }

            UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(username);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            String newToken = jwtUtils.generateJwtToken(authentication);

            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            logger.debug("Token refreshed successfully for user: {}", username);

            return ResponseEntity.ok(new JwtResponse(
                    newToken,
                    userDetails.getId(),
                    userDetails.getUsername(),
                    userDetails.getEmail(),
                    roles,
                    userDetails.getDepartment()));

        } catch (Exception e) {
            logger.error("Error refreshing token", e); // Log full stacktrace
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error refreshing token"));
        }
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwt".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
