# No Dues - Backend

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

- **Secure REST API:** Secured using Spring Security and JSON Web Tokens (JWT) for stateless, token-based authentication.
- **User & Role Management:** Role-based access control (RBAC) for different user types (e.g., Admin, Department Head).
- **Comprehensive Data Management:** Full CRUD (Create, Read, Update, Delete) operations for Students, Users, Departments, and Dues.
- **Certificate Workflow Engine:** Manages the state and approval process for No Dues certificate requests.
- **Dashboard Analytics:** An endpoint to provide aggregated statistics for the frontend dashboard.
- **Email Notifications:** Integrated email service for sending user credentials and notifications.
- **Containerized:** Comes with a `Dockerfile` for easy, consistent deployment.

---

## 🛠️ Tech Stack & Tools

- **Framework:** Spring Boot
- **Language:** Java 17
- **Security:** Spring Security, JWT
- **Database:** Spring Data JPA (compatible with PostgreSQL, MySQL, etc.)
- **Build Tool:** Apache Maven
- **Containerization:** Docker

---

## 🏛️ Project Structure Explained

## The project follows the standard conventions for a Spring Boot application, promoting a clean and layered architecture.

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

## 🔑 API Endpoints

### Auth Controller

| Method | Endpoint | Description | Authorization |
| --- | --- | --- | --- |
| POST | /api/auth/signin | Authenticate user and return JWT token | Public |
| POST | /api/auth/forgot-password | Request a password reset | Public |
| POST | /api/auth/reset-password | Reset password using a token | Public |
| POST | /api/auth/refresh-token | Refresh JWT token | Public |

### Dashboard Controller

| Method | Endpoint | Description | Authorization |
| --- | --- | --- | --- |
| GET | /api/dashboard/stats | Get dashboard statistics | ADMIN, HOD, DEPARTMENT_ADMIN, PRINCIPAL, STUDENT |

### Department Controller

| Method | Endpoint | Description | Authorization |
| --- | --- | --- | --- |
| GET | /api/departments | Get all departments | Public |
| GET | /api/departments/{id} | Get a department by ID | Public |
| POST | /api/departments | Create a new department | ADMIN |
| PUT | /api/departments/{id} | Update an existing department | ADMIN |
| DELETE | /api/departments/{id} | Delete a department | ADMIN |
| GET | /api/departments/list | Get a list of all department enums | Public |

### Due Controller

| Method | Endpoint | Description | Authorization |
| --- | --- | --- | --- |
| GET | /api/dues | Get all dues based on user role | ADMIN, DEPARTMENT_ADMIN, HOD, PRINCIPAL, STUDENT |
| GET | /api/dues/student/{studentId} | Get all dues for a specific student | ADMIN, DEPARTMENT_ADMIN, HOD, PRINCIPAL, or current user |
| GET | /api/dues/{id} | Get a specific due by ID | ADMIN, DEPARTMENT_ADMIN, HOD, PRINCIPAL, or user with access |
| POST | /api/dues | Create a new due | ADMIN, DEPARTMENT_ADMIN |
| PUT | /api/dues/{id}/pay | Pay a due | STUDENT |
| PUT | /api/dues/{id}/approve | Approve a paid due | ADMIN, DEPARTMENT_ADMIN |
| PUT | /api/dues/{id}/reject | Reject a paid due | ADMIN, DEPARTMENT_ADMIN |
| DELETE | /api/dues/{id} | Delete a due | ADMIN, DEPARTMENT_ADMIN |
| POST | /api/dues/{id}/generate-receipt | Generate a receipt for an approved due | ADMIN, DEPARTMENT_ADMIN |

### Health Controller

| Method | Endpoint | Description | Authorization |
| --- | --- | --- | --- |
| GET | /health | Health check | Public |

### No Dues Certificate Controller

| Method | Endpoint | Description | Authorization |
| --- | --- | --- | --- |
| GET | /api/certificates | Get all no-dues certificates | Public |
| POST | /api/certificates/request/{rollNumber} | Request a no-dues certificate | Public |
| POST | /api/certificates/student/{studentId} | Create a no-dues certificate for a student | ADMIN, HOD |
| POST | /api/certificates/student/roll/{rollNumber} | Create a no-dues certificate by roll number | ADMIN, HOD |
| GET | /api/certificates/{id} | Get a no-dues certificate by ID | ADMIN, DEPARTMENT_ADMIN, HOD, PRINCIPAL, STUDENT |
| GET | /api/certificates/number/{certificateNumber} | Get a no-dues certificate by certificate number | ADMIN, DEPARTMENT_ADMIN, HOD, PRINCIPAL, STUDENT |
| GET | /api/certificates/student/{studentId} | Get all no-dues certificates for a student | ADMIN, DEPARTMENT_ADMIN, HOD, PRINCIPAL, or current user |
| GET | /api/certificates/status/{status} | Get no-dues certificates by status | ADMIN, HOD, PRINCIPAL |
| GET | /api/certificates/pending-principal | Get certificates pending principal signature | ADMIN, PRINCIPAL |
| POST | /api/certificates/{id}/sign-principal | Sign a certificate by the principal | PRINCIPAL, ADMIN |
| POST | /api/certificates/{id}/sign-department | Sign a certificate by a department | DEPARTMENT_ADMIN, ADMIN, HOD |
| POST | /api/certificates/{id}/request-department-signature | Request a signature from a department | DEPARTMENT_ADMIN, HOD |
| GET | /api/certificates/pending-signatures/{department} | Get pending signatures for a department | ADMIN, DEPARTMENT_ADMIN, HOD |
| GET | /api/certificates/check-eligibility/{studentId} | Check if a student is eligible for a no-dues certificate | ADMIN, DEPARTMENT_ADMIN, HOD, PRINCIPAL, or current user |
| GET | /api/certificates/students-with-cleared-dues | Get students with all dues cleared | ADMIN, HOD, PRINCIPAL |
| POST | /api/certificates/department-receipt/{studentId} | Generate a department-specific receipt | ADMIN, DEPARTMENT_ADMIN, HOD |
| DELETE | /api/certificates/{id} | Delete a no-dues certificate | ADMIN |

### Profile Controller

| Method | Endpoint | Description | Authorization |
| --- | --- | --- | --- |
| PUT | /api/profile | Update user profile | Authenticated |
| GET | /api/profile | Get user profile | Authenticated |
| POST | /api/profile/signature | Upload user signature | Authenticated |
| GET | /api/profile/signature | Get user signature | Authenticated |

### Student Controller

| Method | Endpoint | Description | Authorization |
| --- | --- | --- | --- |
| GET | /api/students | Get all students | ADMIN, HOD, DEPARTMENT_ADMIN, PRINCIPAL |
| GET | /api/students/department/{department} | Get students by department | ADMIN, HOD, DEPARTMENT_ADMIN |
| GET | /api/students/{id} | Get a student by ID | ADMIN, HOD, DEPARTMENT_ADMIN |
| GET | /api/students/user/{userId} | Get a student by user ID | ADMIN, HOD, DEPARTMENT_ADMIN, STUDENT |
| GET | /api/students/roll/{rollNumber} | Get a student by roll number | ADMIN, HOD, DEPARTMENT_ADMIN |
| POST | /api/students | Create a new student | ADMIN, HOD |
| PUT | /api/students/{id} | Update an existing student | ADMIN, HOD |
| DELETE | /api/students/{id} | Delete a student | ADMIN |

### User Controller

| Method | Endpoint | Description | Authorization |
| --- | --- | --- | --- |
| GET | /api/users | Get all users | ADMIN, HOD |
| GET | /api/users/{id} | Get a user by ID | ADMIN, HOD |
| POST | /api/users | Create a new user | ADMIN |
| PUT | /api/users/{id} | Update an existing user | ADMIN |
| DELETE | /api/users/{id} | Delete a user | ADMIN |
| POST | /api/users/{id}/signature | Upload a signature for a user | ADMIN, HOD, or current user |
| GET | /api/users/{id}/signature | Get a user's signature | ADMIN, HOD, or current user |

---

##  Prerequisites

*   **Java 21**: Make sure you have Java 21 installed.
*   **Maven**: This project uses Maven for dependency management.
*   **Docker (Optional)**: For running the application in a containerized environment.

## 🚀 Installation and Setup

### Without Docker

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/your-username/grvmehto-18-no_due_backend.git
    cd grvmehto-18-no_due_backend
    ```
2.  **Database Configuration:**
    *   Open the `src/main/resources/application.properties` file.
    *   Update the `spring.datasource.url`, `spring.datasource.username`, and `spring.datasource.password` properties to match your database configuration.
3.  **Run the application:**
    ```bash
    mvn spring-boot:run
    ```
    The application will be accessible at `http://localhost:8080`.

### With Docker

1.  **Build the Docker image:**
    ```bash
    docker build -t no-due-backend .
    ```
2.  **Run the Docker container:**
    ```bash
    docker run -d -p 8080:8080 -e "SPRING_PROFILES_ACTIVE=docker" --name no-due-container no-due-backend
    ```
    The application will be accessible at `http://localhost:8080`.

## <caption>API Usage Examples</caption>

Here are a few examples of how to use the API with `curl`:

*   **Authenticate a user:**
    ```bash
    curl -X POST http://localhost:8080/api/auth/signin -H "Content-Type: application/json" -d '{"username": "user@example.com", "password": "password"}'
    ```
*   **Get all students (requires authentication):**
    ```bash
    curl -X GET http://localhost:8080/api/students -H "Authorization: Bearer <your_jwt_token>"
    ```

## 🚨 Error Handling

The API uses standard HTTP status codes to indicate the success or failure of a request. In case of an error, the response body will contain a JSON object with an `error` message.

*   **400 Bad Request**: The request was invalid or cannot be otherwise served.
*   **401 Unauthorized**: Authentication is required and has failed or has not yet been provided.
*   **403 Forbidden**: The server understood the request but refuses to authorize it.
*   **404 Not Found**: The requested resource could not be found.
*   **500 Internal Server Error**: An unexpected condition was encountered on the server.

## 🧪 Testing

To run the tests for this project, use the following command:

```bash
mvn test
```