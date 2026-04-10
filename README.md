# 📝 Fundoo Notes

A production-style, student-friendly **note-taking application backend** built with Spring Boot.  
Inspired by Google Keep — enabling users to create, organize, and manage their personal notes through a clean REST API.

---

> **⚠️ Branch Strategy Notice**  
> This `main` branch is **documentation-only by design**. It contains only this README and project overview.  
> The runnable backend implementation lives in the **`develop`** branch.  
> See [Branch Strategy](#branch-strategy) below for details.

---

## 🏗️ Architecture Overview

The application follows a strict **layered architecture** with clean separation of concerns:

```
Client Request
  → Controller          (REST endpoint, request mapping, validation)
  → Service Layer       (business logic, ownership checks)
  → Repository Layer    (data persistence via Spring Data JPA)
  → Database            (MySQL)

Cross-cutting:
  ├── Config            (SecurityConfig, externalized properties)
  ├── Security          (JwtUtil — token generation/validation)
  ├── Exception         (GlobalExceptionHandler, custom exceptions)
  ├── Mapper            (EntityDtoMapper — entity ↔ DTO conversion)
  └── DTO               (request/response objects — never expose entities)
```

### Design Principles
- **Controllers are thin** — they only expose endpoints and delegate to services
- **Services contain business logic** — validation, ownership checks, state transitions
- **Entities never leak to API responses** — all output goes through DTOs
- **Mapper utility centralizes conversions** — easy to extend for Part 2/3
- **Exceptions are structured** — global handler returns consistent error JSON

---

## 📦 Package Structure

```
com.fundoonotes
├── FundooNotesApplication.java
├── config/
│   └── SecurityConfig.java              # PasswordEncoder, permit-all chain
├── controller/
│   ├── UserController.java              # /api/users endpoints
│   └── NoteController.java              # /api/notes endpoints
├── dto/
│   ├── request/
│   │   ├── UserRegisterRequestDto.java  # Registration input
│   │   ├── LoginRequestDto.java         # Login input
│   │   └── NoteRequestDto.java          # Note creation input
│   └── response/
│       ├── UserResponseDto.java         # User data output
│       ├── LoginResponseDto.java        # JWT token output
│       ├── NoteResponseDto.java         # Note data output
│       └── ErrorResponse.java           # Structured error output
├── entity/
│   ├── User.java                        # JPA entity — users table
│   └── Note.java                        # JPA entity — notes table
├── exception/
│   ├── GlobalExceptionHandler.java      # @RestControllerAdvice
│   ├── UserAlreadyExistsException.java  # 409
│   ├── UserNotFoundException.java       # 404
│   ├── InvalidCredentialsException.java # 401
│   ├── NoteNotFoundException.java       # 404
│   └── UnauthorizedAccessException.java # 403
├── mapper/
│   └── EntityDtoMapper.java             # Static mapping methods
├── repository/
│   ├── UserRepository.java
│   └── NoteRepository.java
├── security/
│   └── JwtUtil.java                     # JWT generation/validation
└── service/
    ├── UserService.java                 # Interface
    ├── NoteService.java                 # Interface
    └── impl/
        ├── UserServiceImpl.java
        └── NoteServiceImpl.java
```

---

## 🌿 Branch Strategy

| Branch | Purpose | Contains |
|--------|---------|----------|
| `main` | **Documentation only** | README, project overview, roadmap |
| `develop` | **Working codebase** | Full Part 1 Spring Boot implementation |
| `feature/part1/fundoo-notes-backend-foundation` | **Feature history** | Preserved commit history for Part 1 |

This follows **Git Flow** methodology:
1. Features are developed on `feature/*` branches
2. Completed features merge into `develop`
3. `main` holds only release-ready documentation and summaries
4. Feature branches are preserved with `-k` flag for history

---

## 🛠️ Tech Stack

| Technology | Purpose |
|-----------|---------|
| Java 21 | Language target |
| Spring Boot 3.5.0 | Application framework |
| Spring Web | REST API |
| Spring Data JPA | Data persistence |
| Spring Validation | Bean validation (JSR 380) |
| Spring Security | PasswordEncoder (BCrypt) |
| MySQL | Relational database |
| JJWT 0.12.6 | JWT token handling |
| Lombok | Boilerplate reduction |
| Maven | Build tool |

---

## ✅ Part 1 — Implemented Features

### User Management
- User registration with email uniqueness check
- Secure password storage (BCrypt encoding)
- User login with JWT token generation
- Input validation (email format, password length)

### Note Management
- Create notes linked to authenticated user
- Retrieve all notes for authenticated user
- **Explicit state actions** (not toggles):
  - Pin / Unpin
  - Archive / Unarchive
  - Trash / Restore

### Security
- JWT token generation (HMAC-SHA256 via JJWT)
- Token validation and user ID extraction
- Note ownership enforcement (users cannot access others' notes)
- Missing/invalid token handling

### Error Handling
- Global exception handler (`@RestControllerAdvice`)
- Structured JSON error responses
- Handles: validation errors, duplicate email, invalid credentials, note not found, unauthorized access

### Logging
- SLF4J structured logging at INFO/DEBUG/WARN/ERROR levels
- No sensitive data (passwords, tokens) logged

---

## 🔌 API Summary

### User Endpoints

| Method | Endpoint | Description | Status |
|--------|----------|-------------|--------|
| POST | `/api/users/register` | Register a new user | 201 |
| POST | `/api/users/login` | Login and get JWT token | 200 |

### Note Endpoints

All note endpoints require `Authorization` header with a valid JWT token.

| Method | Endpoint | Description | Status |
|--------|----------|-------------|--------|
| POST | `/api/notes` | Create a new note | 201 |
| GET | `/api/notes` | Get all notes for user | 200 |
| PATCH | `/api/notes/{id}/pin` | Pin a note | 200 |
| PATCH | `/api/notes/{id}/unpin` | Unpin a note | 200 |
| PATCH | `/api/notes/{id}/archive` | Archive a note | 200 |
| PATCH | `/api/notes/{id}/unarchive` | Unarchive a note | 200 |
| PATCH | `/api/notes/{id}/trash` | Trash a note | 200 |
| PATCH | `/api/notes/{id}/restore` | Restore from trash | 200 |

### Error Responses

All errors return structured JSON:
```json
{
  "timestamp": "2026-04-10 11:22:00",
  "status": 409,
  "error": "Conflict",
  "message": "User with email user@test.com already exists",
  "path": "/api/users/register"
}
```

---

## 🚀 Build & Run Instructions

### Prerequisites
- Java 21+
- Maven 3.8+
- MySQL 8.0+

### Database Setup
```sql
CREATE DATABASE IF NOT EXISTS fundoo_notes;
```

### Run the Application
```bash
# Clone and switch to develop branch
git clone https://github.com/smanaspratap/FundooNotes_SpringBoot.git
cd FunduNotes
git checkout develop

# Set environment variables for your local MySQL
export DB_USERNAME=root
export DB_PASSWORD=your_password
export JWT_SECRET=YourSecureSecretKeyAtLeast32CharsLong

# Run
./mvnw spring-boot:run
```

The application starts on `http://localhost:8080`.

### Configuration
All config is externalized in `application.properties`. Override sensitive values via:
- **Environment variables**: `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `JWT_EXPIRATION`
- **Local profile**: Create `application-local.properties` (git-ignored) for dev overrides

---

## 🗺️ Roadmap

### Part 1 — Backend Foundation ✅ (Current)
- Spring Boot project setup
- User registration & login with JWT
- Note CRUD with state transitions
- DTO-based API design
- Exception handling & logging
- Clean architecture foundation

### Part 2 — Advanced Backend (Upcoming)
- Labels for notes (many-to-many)
- Reminders with scheduling
- Elasticsearch integration for search
- Redis caching layer
- RabbitMQ for async operations
- Email notifications
- Spring Security JWT filter chain
- Pagination and sorting

### Part 3 — Full Stack Integration (Future)
- Frontend (React/Angular)
- File attachments (cloud storage)
- Real-time collaboration
- OAuth2 social login
- CI/CD pipeline
- Docker containerization
- Production deployment

---

## 📄 License

This project is developed for educational purposes as part of the BridgeLabz training program.

---

*Built by Manas Pratap Singh*
