package com.college.duemanagement.service;

import com.college.duemanagement.entity.User;
import com.college.duemanagement.repository.UserRepository;
import com.college.duemanagement.repository.RoleRepository;
import com.college.duemanagement.entity.Role;
import com.college.duemanagement.payload.request.CreateUserRequest;
import com.college.duemanagement.payload.request.UpdateUserRequest;
import com.college.duemanagement.payload.request.ProfileUpdateRequest;
import com.college.duemanagement.payload.response.JwtResponse;
import com.college.duemanagement.exception.ResourceNotFoundException;
import com.college.duemanagement.exception.BadRequestException;
import com.college.duemanagement.security.services.UserDetailsImpl;
import com.college.duemanagement.security.jwt.JwtUtils;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;

@Service
public class UserService {

     private final UserRepository userRepository;
     private final RoleRepository roleRepository;
     private final PasswordEncoder passwordEncoder;
     private final EmailService emailService;
     private final AuthenticationManager authenticationManager;
     private final JwtUtils jwtUtils;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, EmailService emailService, AuthenticationManager authenticationManager, JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    public List<User> getAllUsers() {
        // Get current user details
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<User> users = new ArrayList<>();

        // If user is ADMIN, return all users
        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            users = userRepository.notExistsRoleStudent();
        }else if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_HOD"))) {
            users = userRepository.findByDepartment(userDetails.getDepartment());
        }

        for(User user : users){
            user.setPassword("");
        }
        return users;
    }

    public User getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.setPassword("");
        // Get current user details
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();


        // If user is HOD, check if requested user is from their department
        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_HOD"))) {
            if (!user.getDepartment().equals(userDetails.getDepartment())) {
                throw new AccessDeniedException("You can only view users from your department");
            }
        }

        return user;
    }

    private Set<Role> convertRoles(Set<String> strRoles) {
        Set<Role> roles = new HashSet<>();
        if (strRoles == null) {
            return roles;
        }

        for (String roleStr : strRoles) {
            try {
                Role.ERole roleName = Role.ERole.valueOf(roleStr);
                Role roleEntity = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new RuntimeException("Error: Role " + roleStr + " is not found."));
                roles.add(roleEntity);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Error: Role " + roleStr + " is not valid.");
            }
        }
        return roles;
    }

    @Transactional
    public JwtResponse createUser(CreateUserRequest request) {
        // Check if email is already in use
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already in use");
        }

        // Generate unique username based on first name and last name
        String baseUsername = generateBaseUsername(request.getFirstName(), request.getLastName());
        String username = generateUniqueUsername(baseUsername);

        // Generate random password
        String password = generateRandomPassword();

        // Generate unique code (student/employee ID)
        String uniqueCode = generateUniqueCode(request.getDepartment());

        // Create new user
        User user = new User();
        user.setUsername(username);
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(password));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setDepartment(request.getDepartment());
        user.setRoles(convertRoles(request.getRoles()));
        user.setUniqueCode(uniqueCode);

        // Save user
        user = userRepository.save(user);

        // Send credentials via email asynchronously
        sendUserCredentialsAsync(user, password);

        // Generate JWT token
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(username, password)
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        // Return JwtResponse
        return new JwtResponse(
            jwt,
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getRoles(),
            user.getDepartment(),
            user.getUniqueCode()
        );
    }

    @Async
    public void sendUserCredentialsAsync(User user, String password) {
        emailService.sendUserCredentials(user, password);
    }

    @Transactional
    public User updateUser(Long id, UpdateUserRequest request) {
        User user = getUserById(id);

        // Check if email is already in use by another user
        if (!user.getEmail().equals(request.getEmail()) &&
            userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already in use");
        }

        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setDepartment(request.getDepartment());
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            // Convert String roles to Role entities
            Set<Role> roleEntities = convertRoles(request.getRoles());
            user.setRoles(roleEntities);
        }

        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Transactional
    public User updateProfile(Long userId, ProfileUpdateRequest request) {
        User user = getUserById(userId);

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadCredentialsException("Current password is incorrect");
        }

        // Check if email is already in use by another user
        if (!user.getEmail().equals(request.getEmail()) &&
            userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already in use");
        }

        // Update user details
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());

        // Update password if provided
        if (request.getNewPassword() != null && !request.getNewPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }

        return userRepository.save(user);
    }

    private String generateBaseUsername(String firstName, String lastName) {
        // Convert to lowercase and remove spaces
        String base = (firstName.charAt(0) + lastName).toLowerCase().replaceAll("\\s+", "");
        // Remove special characters
        return base.replaceAll("[^a-zA-Z0-9]", "");
    }

    private String generateUniqueUsername(String baseUsername) {
        String username = baseUsername;
        int counter = 1;

        while (userRepository.existsByUsername(username)) {
            username = baseUsername + counter;
            counter++;
        }

        return username;
    }

    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        StringBuilder password = new StringBuilder();
        Random random = new Random();

        // Generate a password with at least:
        // 1 uppercase letter, 1 lowercase letter, 1 number, 1 special character
        password.append(chars.substring(0, 26).charAt(random.nextInt(26))); // Uppercase
        password.append(chars.substring(26, 52).charAt(random.nextInt(26))); // Lowercase
        password.append(chars.substring(52, 62).charAt(random.nextInt(10))); // Number
        password.append(chars.substring(62).charAt(random.nextInt(8))); // Special

        // Add 4 more random characters
        for (int i = 0; i < 4; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }

        // Shuffle the password
        char[] passwordArray = password.toString().toCharArray();
        for (int i = passwordArray.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = passwordArray[i];
            passwordArray[i] = passwordArray[j];
            passwordArray[j] = temp;
        }

        return new String(passwordArray);
    }

    private String generateUniqueCode(String department) {
        // Generate a unique code for the user based on department
        String prefix = department.substring(0, Math.min(3, department.length())).toUpperCase();
        String randomPart = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return prefix + "-" + randomPart;
    }

    @Transactional
    public void saveSignature(Long userId, byte[] signatureData) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setESignature(signatureData);
        userRepository.save(user);
    }

    public User createUser(@NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email,
                         @NotBlank(message = "First name is required") String firstName,
                         @NotBlank(message = "Last name is required") String lastName,
                         @NotBlank(message = "Department is required") String department,
                         Set<String> roles) {
        // Check if email is already in use
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email is already in use");
        }

        // Generate unique username based on first name and last name
        String baseUsername = generateBaseUsername(firstName, lastName);
        String username = generateUniqueUsername(baseUsername);

        // Generate random password
        String password = generateRandomPassword();

        // Generate unique code (student/employee ID)
        String uniqueCode = generateUniqueCode(department);

        // Create new user
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setDepartment(department);
        user.setRoles(convertRoles(roles));
        user.setUniqueCode(uniqueCode);

        // Save user
        user = userRepository.save(user);

        // Send credentials via email
        emailService.sendUserCredentials(user, password);

        return user;
    }
}