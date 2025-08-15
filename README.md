# No Dues Management System - Backend

![Java](https://img.shields.io/badge/Java-17-blue?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-Authentication-black?style=for-the-badge&logo=jsonwebtokens&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)

---

## Overview

This repository contains the backend service for the **No Dues Management System**. It's a robust RESTful API built with Java and the Spring Boot framework. This service is responsible for all the core business logic, data persistence, and security of the application.

It provides endpoints for user authentication, managing students, departments, and dues, and orchestrating the multi-step No Dues certificate approval workflow.

---

## ✨ Key Features

* **Secure REST API:** Secured using Spring Security and JSON Web Tokens (JWT) for stateless, token-based authentication.
* **User & Role Management:** Role-based access control (RBAC) for different user types (e.g., Admin, Department Head).
* **Comprehensive Data Management:** Full CRUD (Create, Read, Update, Delete) operations for Students, Users, Departments, and Dues.
* **Certificate Workflow Engine:** Manages the state and approval process for No Dues certificate requests.
* **Dashboard Analytics:** An endpoint to provide aggregated statistics for the frontend dashboard.
* **Email Notifications:** Integrated email service for sending user credentials and notifications.
* **Containerized:** Comes with a `Dockerfile` for easy, consistent deployment.

---

## 🛠️ Tech Stack & Tools

* **Framework:** Spring Boot
* **Language:** Java 17
* **Security:** Spring Security, JWT
* **Database:** Spring Data JPA (compatible with PostgreSQL, MySQL, etc.)
* **Build Tool:** Apache Maven
* **Containerization:** Docker

---

## 🏛️ Project Structure Explained

The project follows the standard conventions for a Spring Boot application, promoting a clean and layered architecture.
---
```
Directory structure:
└── main/
    ├── java/
    │   └── com/
    │       └── college/
    │           └── duemanagement/
    │               ├── DueManagementApplication.java
    │               ├── controller/
    │               │   ├── AuthController.java
    │               │   ├── DashboardController.java
    │               │   ├── DepartmentController.java
    │               │   ├── DueController.java
    │               │   ├── NoDuesCertificateController.java
    │               │   ├── ProfileController.java
    │               │   ├── StudentController.java
    │               │   └── UserController.java
    │               ├── dto/
    │               │   ├── ActivityDto.java
    │               │   ├── CreateDepartmentRequest.java
    │               │   ├── CreateDueRequest.java
    │               │   ├── CreateUserRequest.java
    │               │   ├── DashboardStatsDto.java
    │               │   ├── DepartmentDto.java
    │               │   ├── DepartmentSignatureDto.java
    │               │   ├── DepartmentStatDto.java
    │               │   ├── DueDto.java
    │               │   ├── JwtResponse.java
    │               │   ├── LoginRequest.java
    │               │   ├── MessageResponse.java
    │               │   ├── NoDueCertificateDto.java
    │               │   ├── NoDuesCertificateDto.java
    │               │   └── UserDto.java
    │               ├── entity/
    │               │   ├── Department.java
    │               │   ├── DepartmentSignature.java
    │               │   ├── Due.java
    │               │   ├── NoDuesCertificate.java
    │               │   ├── Role.java
    │               │   ├── Student.java
    │               │   └── User.java
    │               ├── enums/
    │               │   └── DepartmentEnum.java
    │               ├── exception/
    │               │   ├── BadRequestException.java
    │               │   ├── GlobalExceptionHandler.java
    │               │   └── ResourceNotFoundException.java
    │               ├── payload/
    │               │   ├── request/
    │               │   │   ├── CreateStudentRequest.java
    │               │   │   ├── CreateUserRequest.java
    │               │   │   ├── ProfileUpdateRequest.java
    │               │   │   ├── UpdateStudentRequest.java
    │               │   │   └── UpdateUserRequest.java
    │               │   └── response/
    │               │       └── JwtResponse.java
    │               ├── repository/
    │               │   ├── DepartmentRepository.java
    │               │   ├── DepartmentSignatureRepository.java
    │               │   ├── DueRepository.java
    │               │   ├── NoDuesCertificateRepository.java
    │               │   ├── RoleRepository.java
    │               │   ├── StudentRepository.java
    │               │   └── UserRepository.java
    │               ├── response/
    │               │   └── ApiResponse.java
    │               ├── security/
    │               │   ├── WebSecurityConfig.java
    │               │   ├── jwt/
    │               │   │   ├── AuthEntryPointJwt.java
    │               │   │   ├── AuthTokenFilter.java
    │               │   │   └── JwtUtils.java
    │               │   └── services/
    │               │       ├── UserDetailsImpl.java
    │               │       └── UserDetailsServiceImpl.java
    │               └── service/
    │                   ├── DueService.java
    │                   ├── EmailService.java
    │                   ├── NoDuesCertificateService.java
    │                   ├── StudentService.java
    │                   ├── UserService.java
    │                   └── impl/
    │                       ├── DueServiceImpl.java
    │                       └── NoDuesCertificateServiceImpl.java
    └── resources/
        ├── application-deploy.properties
        ├── application-docker.properties
        ├── application.properties
        ├── META-INF/
        │   └── additional-spring-configuration-metadata.json
        └── templates/
            └── email/
                └── credentials.html
```
---

## 🔑 API Endpoints Overview

The API is organized by resource. Here is a high-level summary of the available endpoints:

* **Authentication (`/api/auth`)**: Handles user login and registration.
* **Users (`/api/users`)**: CRUD operations for managing system users.
* **Students (`/api/students`)**: CRUD operations for managing student records.
* **Departments (`/api/departments`)**: Manages college departments.
* **Dues (`/api/dues`)**: Manages the creation, updating, and payment of student dues.
* **Certificates (`/api/certificates`)**: Manages the No Dues certificate request and approval workflow.
* **Dashboard (`/api/dashboard`)**: Provides summary data and statistics.
* **Profile (`/api/profile`)**: Allows users to view and update their own profiles.

> #### **⭐ Best Practice: API Documentation**
> For detailed, interactive API documentation, it is highly recommended to add the **SpringDoc OpenAPI** dependency to your `pom.xml`. This will automatically generate a Swagger UI page where you can view and test all endpoints live.
>
> ```xml
> <dependency>
>     <groupId>org.springdoc</groupId>
>     <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
>     <version>2.5.0</version>
> </dependency>
> ```

---

## ⚙️ Configuration

Application settings are managed in `src/main/resources/application.properties`. For local development, you should create a new profile (e.g., `application-dev.properties`) or override the default properties.

**Key properties to configure:**

```properties
# --- Database Configuration ---
# Example for PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/your_db_name
spring.datasource.username=your_db_user
spring.datasource.password=your_db_password
spring.jpa.hibernate.ddl-auto=update # Use 'validate' in production

# --- JWT Secret ---
# A strong, long, random secret key for signing JWTs
app.jwtSecret=your-super-secret-key-that-is-very-long
app.jwtExpirationMs=86400000 # 24 hours

# --- Email Server (SMTP) ---
spring.mail.host=smtp.example.com
spring.mail.port=587
spring.mail.username=your-email@example.com
spring.mail.password=your-email-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# --- Getting Started ---

# --- This script automates the process of cloning, configuring, and running the backend project in Docker. ---

# Step 1: Clone the repository and navigate into it
# ▼▼▼ REPLACE THIS URL WITH YOUR ACTUAL REPOSITORY URL ▼▼▼
git clone https://github.com/your-username/grvmehto-18-no_due_backend.git
cd grvmehto-18-no_due_backend

echo "✅ Repository cloned."

# -----------------------------------------------------------------------------------

# Step 2: Create the Docker-specific configuration file using a 'here document'
# This command creates the file and populates it with the necessary settings.
# ▼▼▼ REPLACE THE DATABASE DETAILS BELOW ▼▼▼
cat <<EOF > src/main/resources/application-docker.properties
# --- Database Configuration for Docker ---
# 'host.docker.internal' is a special DNS name that resolves to your host machine's IP from inside a Docker container.
spring.datasource.url=jdbc:postgresql://host.docker.internal:5432/your_db_name
spring.datasource.username=your_db_user
spring.datasource.password=your_db_password

# It's good practice to set this to 'validate' for production-like environments
spring.jpa.hibernate.ddl-auto=validate
EOF

echo "⚙️  Docker configuration file created."

# -----------------------------------------------------------------------------------

# Step 3: Build the Docker image using the Dockerfile in the current directory
echo "📦 Building Docker image..."
docker build -t no-due-backend .

echo "✅ Docker image built successfully."

# -----------------------------------------------------------------------------------

# Step 4: Run the newly built image as a container
# It runs in detached mode, maps port 8080, and activates the 'docker' profile.
echo "▶️  Running Docker container..."
docker run -d -p 8080:8080 -e "SPRING_PROFILES_ACTIVE=docker" --name no-due-container no-due-backend

# -----------------------------------------------------------------------------------

# Step 5: Verify that the container is running and check the logs
echo "⏳ Waiting for the container to start..."
sleep 10 # Give the container a few seconds to initialize

echo "--- Verifying Container Status (docker ps) ---"
docker ps

echo "--- Displaying Initial Container Logs (docker logs) ---"
docker logs no-due-container

echo "🚀 Process complete! The backend should be accessible at http://localhost:8080"

