package com.college.duemanagement;

import com.college.duemanagement.entity.Department;
import com.college.duemanagement.entity.Role;
import com.college.duemanagement.entity.User;
import com.college.duemanagement.enums.DepartmentEnum;
import com.college.duemanagement.repository.RoleRepository;
import com.college.duemanagement.repository.UserRepository;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.Collections;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@OpenAPIDefinition(info = @Info(title = "NO DUES API", version = "1.0", description = "API documentation for NO DUES"))
public class DueManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(DueManagementApplication.class, args);
    }
    @Bean
    public CommandLineRunner initData(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder encoder) {
        return args -> {
            // Create roles if they don't exist
            if (roleRepository.count() == 0) {
                roleRepository.saveAll(Arrays.asList(
                        new Role(Role.ERole.ROLE_ADMIN),
                        new Role(Role.ERole.ROLE_DEPARTMENT_ADMIN),
                        new Role(Role.ERole.ROLE_HOD),
                        new Role(Role.ERole.ROLE_PRINCIPAL),
                        new Role(Role.ERole.ROLE_STUDENT)
                ));
            }

            // Create admin user if it doesn't exist
            if (!userRepository.existsByUsername("admin")) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setDepartment(DepartmentEnum.OFFICE.getDisplayName());
                admin.setPassword(encoder.encode("admin123"));
                admin.setEmail("admin@college.com");
                admin.setFirstName("Admin");
                admin.setLastName("User");
                admin.setUniqueCode("ADMIN001");
                admin.setRoles(Collections.singleton(roleRepository.findByName(Role.ERole.ROLE_ADMIN).get()));

                userRepository.save(admin);
            }
        };
    }
} 