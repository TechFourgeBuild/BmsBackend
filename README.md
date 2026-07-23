# BookIt — Movie Booking System · Backend

<div align="center">

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0.3-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6.x-6DB33F?style=flat-square&logo=springsecurity&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.4.8-4479A1?style=flat-square&logo=mysql&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-0.12.6-000000?style=flat-square&logo=jsonwebtokens&logoColor=white)
![Hibernate](https://img.shields.io/badge/Hibernate-7.2.4-59666C?style=flat-square&logo=hibernate&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.9.x-C71A36?style=flat-square&logo=apachemaven&logoColor=white)
![Lombok](https://img.shields.io/badge/Lombok-1.18.44-BC4521?style=flat-square)

</div>

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Technology Stack](#2-technology-stack)
3. [System Architecture](#3-system-architecture)
4. [Project Structure](#4-project-structure)
5. [Database Schema](#5-database-schema)
6. [Security Implementation](#6-security-implementation)
7. [API Reference](#7-api-reference)
8. [Error Handling](#8-error-handling)
9. [Getting Started](#9-getting-started)
10. [Environment Variables](#10-environment-variables)

---

## 1. Project Overview

**BookIt** is a production-grade movie ticket booking platform built on **Spring Boot 4** with a stateless JWT-based security model. It provides a clean RESTful API layer for browsing movies, selecting seats, booking tickets, and managing the full cinema ecosystem through a role-separated admin interface.

### Core Capabilities

| Area | Description |
|------|-------------|
| **Authentication** | JWT-based stateless auth — register, login, token issuance |
| **Movie Management** | Full CRUD — browse, search, filter by genre and language |
| **Booking Engine** | Real-time seat selection, booking creation, and cancellation |
| **Theater Management** | Cities → Theaters → Screens → Seats hierarchy |
| **Show Scheduling** | Showtimes mapped to movies and screens with pricing |
| **Role-Based Access** | `PUBLIC`, `USER`, and `ADMIN` roles with granular endpoint control |
| **Global Error Handling** | Centralized `@RestControllerAdvice` with typed exceptions |

---

## 2. Technology Stack

### Backend Dependencies (from `pom.xml`)

| Dependency | Version | Purpose |
|------------|---------|---------|
| `spring-boot-starter-parent` | **4.0.3** | Core framework & dependency management |
| `spring-boot-starter-webmvc` | 4.0.3 | REST controllers, DispatcherServlet |
| `spring-boot-starter-data-jpa` | 4.0.3 | ORM, repositories, transaction management |
| `spring-boot-starter-security` | 4.0.3 | Authentication & authorization |
| `hibernate-core` | **7.2.4.Final** | ORM engine (SB4-compatible) |
| `jjwt-api` | **0.12.6** | JWT token API |
| `jjwt-impl` | 0.12.6 | JWT implementation (runtime) |
| `jjwt-jackson` | 0.12.6 | JWT JSON serialization (runtime) |
| `mysql-connector-j` | managed | MySQL JDBC driver |
| `lombok` | **1.18.44** | Boilerplate reduction (`@Data`, `@Builder`, etc.) |
| `spring-boot-devtools` | managed | Hot reload during development |
| `spring-boot-starter-test` | managed | JUnit 5 + Mockito |
| `spring-boot-starter-security-test` | managed | Security testing utilities |

### Build Tooling

| Tool | Version |
|------|---------|
| Java | 21 (LTS) |
| Maven | 3.9.x |
| Maven Compiler Plugin | Configured for Java 21 + Lombok annotation processing |
| Spring Boot Maven Plugin | Lombok excluded from final JAR |

---

## 3. System Architecture

### High-Level Layered Architecture

```
┌──────────────────────────────────────────────────────┐
│                    CLIENT LAYER                      │
│   React 19 (Vercel)  ·  Mobile Web  ·  API Clients  │
└──────────────────────────┬───────────────────────────┘
                           │ HTTP / HTTPS
                           ▼
┌──────────────────────────────────────────────────────┐
│                  CONTROLLER LAYER                    │
│                                                      │
│  UserController     MovieController                  │
│  TheaterController  ScreenController                 │
│  SeatController     ShowController                   │
│  BookingController  CityController                   │
└──────────────────────────┬───────────────────────────┘
                           │
                           ▼
┌──────────────────────────────────────────────────────┐
│                  SECURITY LAYER                      │
│                                                      │
│  JwtAuthenticationFilter                             │
│  → validates Bearer token on every request           │
│  → populates SecurityContextHolder                   │
│                                                      │
│  SecurityConfig  ·  JwtUtil  ·  JwtAuthEntryPoint    │
│  CustomUserDetailsService  ·  PasswordConfig         │
└──────────────────────────┬───────────────────────────┘
                           │
                           ▼
┌──────────────────────────────────────────────────────┐
│                   SERVICE LAYER                      │
│                                                      │
│  UserService    MovieService    TheaterService       │
│  ScreenService  SeatService     ShowService          │
│  BookingService CityService                          │
└──────────────────────────┬───────────────────────────┘
                           │
                           ▼
┌──────────────────────────────────────────────────────┐
│               DATA ACCESS LAYER                      │
│                                                      │
│  Spring Data JPA Repositories                        │
│  → UserRepository       MovieRepository              │
│  → TheaterRepository    ScreenRepository             │
│  → SeatRepository       ShowRepository               │
│  → BookingRepository    CityRepository               │
└──────────────────────────┬───────────────────────────┘
                           │
                           ▼
┌──────────────────────────────────────────────────────┐
│                  DATABASE LAYER                      │
│                                                      │
│           MySQL 8.4.8 · Aiven Cloud                  │
└──────────────────────────────────────────────────────┘
```

### Request Lifecycle

```
[1]  Incoming HTTP request
[2]  JwtAuthenticationFilter intercepts
       → extracts Bearer token from Authorization header
       → JwtUtil validates signature + expiry
       → loads UserDetails via CustomUserDetailsService
       → sets Authentication in SecurityContextHolder
[3]  SecurityConfig authorization rules evaluated
       → PUBLIC / USER / ADMIN endpoint matching
[4]  Controller receives request
       → deserializes request DTO
       → delegates to Service layer
[5]  Service executes business logic
       → calls Repository
[6]  Repository executes JPA query via Hibernate
       → returns entity
[7]  Service maps Entity → DTO
[8]  Controller serializes response JSON
[9]  Response returns to client
```

---

## 4. Project Structure

```
BMSProject/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/bms/BMSProject/
│       │       │
│       │       ├── config/                          # Spring configuration beans
│       │       │   ├── CorsConfig.java              # CORS allowed origins & methods
│       │       │   ├── PasswordConfig.java          # BCryptPasswordEncoder bean
│       │       │   └── SecurityConfig.java          # Security filter chain, CSRF, session
│       │       │
│       │       ├── controller/                      # REST API layer
│       │       │   ├── BookingController.java
│       │       │   ├── CityController.java
│       │       │   ├── MovieController.java
│       │       │   ├── ScreenController.java
│       │       │   ├── SeatController.java
│       │       │   ├── ShowController.java
│       │       │   ├── TheaterController.java
│       │       │   └── UserController.java
│       │       │
│       │       ├── dto/                             # Data Transfer Objects
│       │       │   ├── AuthResponse.java            # JWT token + user info on login
│       │       │   ├── BookingRequest.java
│       │       │   ├── LoginRequest.java
│       │       │   ├── ScreenRequest.java
│       │       │   ├── SeatRequest.java
│       │       │   ├── ShowRequest.java
│       │       │   ├── TheaterRequest.java
│       │       │   └── UserRequest.java
│       │       │
│       │       ├── entity/                          # JPA entities (database tables)
│       │       │   ├── Booking.java
│       │       │   ├── City.java
│       │       │   ├── Movie.java
│       │       │   ├── Screen.java
│       │       │   ├── Seat.java
│       │       │   ├── Show.java
│       │       │   ├── Theater.java
│       │       │   └── User.java
│       │       │
│       │       ├── enums/                           # Type-safe constants
│       │       │   ├── BookingStatus.java           # CONFIRMED / CANCELLED
│       │       │   ├── Role.java                   # USER / ADMIN
│       │       │   └── SeatType.java               # REGULAR / PREMIUM / VIP
│       │       │
│       │       ├── exception/                       # Global exception handling
│       │       │   ├── GlobalExceptionHandler.java  # @RestControllerAdvice
│       │       │   ├── BookingException.java
│       │       │   ├── DuplicateResourceException.java
│       │       │   ├── InvalidCredentialsException.java
│       │       │   └── ResourceNotFoundException.java
│       │       │
│       │       ├── repository/                      # Spring Data JPA interfaces
│       │       │   ├── BookingRepository.java
│       │       │   ├── CityRepository.java
│       │       │   ├── MovieRepository.java
│       │       │   ├── ScreenRepository.java
│       │       │   ├── SeatRepository.java
│       │       │   ├── ShowRepository.java
│       │       │   ├── TheaterRepository.java
│       │       │   └── UserRepository.java
│       │       │
│       │       ├── security/
│       │       │   ├── jwt/
│       │       │   │   ├── JwtAuthenticationFilter.java   # OncePerRequestFilter
│       │       │   │   ├── JwtAuthEntryPoint.java         # 401 handler
│       │       │   │   └── JwtUtil.java                   # token generation & validation
│       │       │   └── service/
│       │       │       └── CustomUserDetailsService.java  # UserDetailsService impl
│       │       │
│       │       ├── service/                         # Business logic layer
│       │       │   ├── BookingService.java
│       │       │   ├── CityService.java
│       │       │   ├── MovieService.java
│       │       │   ├── ScreenService.java
│       │       │   ├── SeatService.java
│       │       │   ├── ShowService.java
│       │       │   ├── TheaterService.java
│       │       │   └── UserService.java
│       │       │
│       │       └── BmsProjectApplication.java       # Spring Boot entry point
│       │
│       └── resources/
│           ├── static/
│           ├── templates/
│           └── application.properties               # DB, JWT, JPA configuration
│                                            
├── pom.xml
├── .gitignore
└── README.md
```

---

## 5. Database Schema

### Entity Relationship Overview

```
City (1) ──────────── (N) Theater
Theater (1) ────────── (N) Screen
Screen (1) ─────────── (N) Seat
Screen (1) ─────────── (N) Show
Movie (1) ──────────── (N) Show
Show (1) ───────────── (N) Booking
User (1) ───────────── (N) Booking
Booking (M) ─────────── (N) Seat   [booking_seats join table]
```

### Table Definitions

#### `users`
| Column | Type | Constraints |
|--------|------|-------------|
| `id` | BIGINT | PK, AUTO_INCREMENT |
| `name` | VARCHAR | NOT NULL |
| `email` | VARCHAR | UNIQUE, NOT NULL |
| `password` | VARCHAR | BCrypt hashed |
| `phone` | VARCHAR | — |
| `role` | ENUM | `USER` / `ADMIN` |
| `created_at` | DATETIME | — |

#### `cities`
| Column | Type | Constraints |
|--------|------|-------------|
| `id` | BIGINT | PK |
| `name` | VARCHAR | NOT NULL |
| `state` | VARCHAR | — |

#### `theaters`
| Column | Type | Constraints |
|--------|------|-------------|
| `id` | BIGINT | PK |
| `city_id` | BIGINT | FK → `cities.id` |
| `name` | VARCHAR | NOT NULL |
| `address` | VARCHAR | — |

#### `screens`
| Column | Type | Constraints |
|--------|------|-------------|
| `id` | BIGINT | PK |
| `theater_id` | BIGINT | FK → `theaters.id` |
| `name` | VARCHAR | NOT NULL |
| `total_seats` | INT | NOT NULL |

#### `seats`
| Column | Type | Constraints |
|--------|------|-------------|
| `id` | BIGINT | PK |
| `screen_id` | BIGINT | FK → `screens.id` |
| `seat_number` | VARCHAR | NOT NULL |
| `row` | VARCHAR | — |
| `col` | INT | — |
| `seat_type` | ENUM | `REGULAR` / `PREMIUM` / `VIP` |

#### `movies`
| Column | Type | Constraints |
|--------|------|-------------|
| `id` | BIGINT | PK |
| `title` | VARCHAR | NOT NULL |
| `description` | TEXT | — |
| `genre` | VARCHAR | — |
| `language` | VARCHAR | — |
| `duration` | INT | minutes |
| `rating` | DECIMAL | — |
| `release_date` | DATE | — |
| `poster_url` | VARCHAR | — |

#### `shows`
| Column | Type | Constraints |
|--------|------|-------------|
| `id` | BIGINT | PK |
| `movie_id` | BIGINT | FK → `movies.id` |
| `screen_id` | BIGINT | FK → `screens.id` |
| `show_date` | DATE | NOT NULL |
| `start_time` | TIME | NOT NULL |
| `end_time` | TIME | — |
| `ticket_price` | DECIMAL | NOT NULL |

#### `bookings`
| Column | Type | Constraints |
|--------|------|-------------|
| `id` | BIGINT | PK |
| `user_id` | BIGINT | FK → `users.id` |
| `show_id` | BIGINT | FK → `shows.id` |
| `total_price` | DECIMAL | NOT NULL |
| `status` | ENUM | `CONFIRMED` / `CANCELLED` |
| `booked_at` | DATETIME | — |

#### `booking_seats` (join table)
| Column | Type | Constraints |
|--------|------|-------------|
| `booking_id` | BIGINT | FK → `bookings.id`, Composite PK |
| `seat_id` | BIGINT | FK → `seats.id`, Composite PK |

### Indexes

| Index Name | Table | Column(s) | Purpose |
|------------|-------|-----------|---------|
| `idx_users_email` | `users` | `email` | Fast login lookup |
| `idx_bookings_user_id` | `bookings` | `user_id` | User booking history |
| `idx_bookings_show_id` | `bookings` | `show_id` | Show-level occupancy |
| `idx_shows_movie_id` | `shows` | `movie_id` | Movie showtimes lookup |
| `idx_shows_screen_id` | `shows` | `screen_id` | Screen schedule lookup |

---

## 6. Security Implementation

### Authentication Flow

```
POST /api/users/register
  → UserRequest DTO received
  → BCrypt password encoding (PasswordConfig)
  → User persisted with role USER
  → JWT token generated via JwtUtil
  → AuthResponse { token, role, name } returned

POST /api/users/login
  → LoginRequest { email, password }
  → AuthenticationManager.authenticate()
  → CustomUserDetailsService.loadUserByUsername()
  → BCrypt match verified
  → JWT token generated
  → AuthResponse returned

Subsequent protected requests:
  → Authorization: Bearer <token> header
  → JwtAuthenticationFilter.doFilterInternal()
      → JwtUtil.extractUsername()
      → JwtUtil.validateToken()
      → CustomUserDetailsService loads user
      → UsernamePasswordAuthenticationToken set in SecurityContext
  → Controller executes
```

### Security Components

| Class | Package | Responsibility |
|-------|---------|----------------|
| `SecurityConfig` | `config` | Filter chain, CORS, CSRF, session policy, authorization rules |
| `JwtAuthenticationFilter` | `security/jwt` | `OncePerRequestFilter` — intercepts every request, validates JWT |
| `JwtUtil` | `security/jwt` | Token generation, claim extraction, expiry validation |
| `JwtAuthEntryPoint` | `security/jwt` | Returns `401 Unauthorized` JSON on unauthenticated access |
| `CustomUserDetailsService` | `security/service` | Implements `UserDetailsService` — loads `User` entity by email |
| `PasswordConfig` | `config` | Defines `BCryptPasswordEncoder` bean |

### Role-Based Access Control

| Role | Scope |
|------|-------|
| `PUBLIC` | `/api/users/register`, `/api/users/login`, all `GET` on movies / shows / theaters / cities |
| `USER` | All `PUBLIC` + create booking, view own bookings, cancel own bookings |
| `ADMIN` | All endpoints — full CRUD on movies, theaters, screens, seats, shows + all user and booking management |

### Security Configuration Summary

| Setting | Value |
|---------|-------|
| Session Management | `STATELESS` — no server-side session |
| CSRF | Disabled (stateless JWT API) |
| Password Encoding | BCrypt |
| Token Algorithm | HS256 (HMAC-SHA256) |
| Token Expiry | 15 minutes |
| CORS | Configured via `CorsConfig` for frontend origin |

---

## 7. API Reference

### Base URL

```
http://localhost:8080/api
```

### Authentication

All protected endpoints require the following header:

```
Authorization: Bearer <jwt_token>
```

---

### User Endpoints `/api/users`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/api/users/register` | PUBLIC | Register new user, returns JWT |
| `POST` | `/api/users/login` | PUBLIC | Authenticate, returns JWT |
| `GET` | `/api/users` | ADMIN | Get all users |
| `GET` | `/api/users/{id}` | ADMIN | Get user by ID |

**Register request:**
```json
{
  "name": "Aman Kumar",
  "email": "aman@example.com",
  "password": "securepassword",
  "phone": "9876543210"
}
```

**Auth response (register & login):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "role": "USER",
  "name": "Aman Kumar"
}
```

---

### Movie Endpoints `/api/movies`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `GET` | `/api/movies` | PUBLIC | Get all movies |
| `GET` | `/api/movies/{id}` | PUBLIC | Get movie by ID |
| `GET` | `/api/movies/search?title={title}` | PUBLIC | Search by title |
| `GET` | `/api/movies/genre/{genre}` | PUBLIC | Filter by genre |
| `GET` | `/api/movies/language/{language}` | PUBLIC | Filter by language |
| `POST` | `/api/movies` | ADMIN | Add new movie |

---

### Theater Endpoints `/api/theaters`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `GET` | `/api/theaters` | PUBLIC | Get all theaters |
| `GET` | `/api/theaters/{id}` | PUBLIC | Get theater by ID |
| `GET` | `/api/theaters/city/{cityId}` | PUBLIC | Theaters in a city |
| `POST` | `/api/theaters` | ADMIN | Add theater |

---

### Screen Endpoints `/api/screens`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `GET` | `/api/screens` | PUBLIC | Get all screens |
| `GET` | `/api/screens/{id}` | PUBLIC | Get screen by ID |
| `GET` | `/api/screens/theater/{theaterId}` | PUBLIC | Screens in a theater |
| `POST` | `/api/screens` | ADMIN | Add screen |
---

### Seat Endpoints `/api/seats`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `GET` | `/api/seats/{id}` | PUBLIC | Get seat by ID |
| `GET` | `/api/seats/screen/{screenId}` | PUBLIC | All seats in a screen |
| `POST` | `/api/seats` | ADMIN | Add seat |

---

### Show Endpoints `/api/shows`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `GET` | `/api/shows` | PUBLIC | Get all shows |
| `GET` | `/api/shows/{id}` | PUBLIC | Get show by ID |
| `GET` | `/api/shows/movie/{movieId}` | PUBLIC | Shows for a movie |
| `GET` | `/api/shows/screen/{screenId}` | PUBLIC | Shows on a screen |
| `GET` | `/api/shows/date/{date}` | PUBLIC | Shows on a date |
| `POST` | `/api/shows` | ADMIN | Schedule a show |
---

### Booking Endpoints `/api/bookings`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `GET` | `/api/bookings/show/{showId}/available-seats` | PUBLIC | Available seats for a show |
| `POST` | `/api/bookings` | USER | Create a booking |
| `GET` | `/api/bookings/{id}` | USER | Get booking by ID |
| `GET` | `/api/bookings/user/{userId}` | USER | Get all bookings for a user |
| `PUT` | `/api/bookings/{id}/cancel` | USER | Cancel a booking |

**Booking request:**
```json
{
  "showId": 5,
  "seatIds": [12, 13, 14],
  "totalPrice": 750.00
}
```

---

### City Endpoints `/api/cities`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `GET` | `/api/cities` | PUBLIC | Get all cities |
| `GET` | `/api/cities/{id}` | PUBLIC | Get city by ID |
| `POST` | `/api/cities` | ADMIN | Add city |

---

### API Coverage Summary

| Controller | Total | PUBLIC | USER | ADMIN |
|------------|-------|--------|------|-------|
| UserController | 4 | 2 | 0 | 2 |
| MovieController | 8 | 5 | 0 | 3 |
| TheaterController | 6 | 3 | 0 | 3 |
| ScreenController | 6 | 3 | 0 | 3 |
| SeatController | 5 | 2 | 0 | 3 |
| ShowController | 8 | 5 | 0 | 3 |
| BookingController | 5 | 1 | 4 | 0 |
| CityController | 5 | 2 | 0 | 3 |
| **Total** | **47** | **23** | **4** | **20** |

---

## 8. Error Handling

All exceptions are handled centrally by `GlobalExceptionHandler` (`@RestControllerAdvice`). Clients always receive a consistent JSON error response.

### Exception Types

| Exception Class | HTTP Status | Trigger Scenario |
|-----------------|-------------|-----------------|
| `ResourceNotFoundException` | `404 Not Found` | Entity not found by ID |
| `DuplicateResourceException` | `409 Conflict` | Duplicate email, title, etc. |
| `BookingException` | `400 Bad Request` | Seat already booked, invalid booking state |
| `InvalidCredentialsException` | `401 Unauthorized` | Wrong email or password on login |
| `JwtAuthEntryPoint` (security) | `401 Unauthorized` | Missing or invalid JWT token |

### Standard Error Response

```json
{
  "timestamp": "2026-07-22T10:30:00",
  "status": 404,
  "error": "Resource Not Found",
  "message": "Movie not found with id: 123",
  "path": "/api/movies/123"
}
```

---

## 9. Getting Started

### Prerequisites

- Java 21
- Maven 3.9+
- MySQL 8.x (local or Aiven Cloud)

### Clone & Build

```bash
git clone https://github.com/your-username/BMSProject.git
cd BMSProject

# Build (skip tests for quick start)
./mvnw clean install -DskipTests
```

### Configure Environment

Create `src/main/resources/application.properties` or set environment variables (see [Section 10](#10-environment-variables)).

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/bookit_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=your_db_user
spring.datasource.password=your_db_password

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# JWT
app.jwt.secret=your-256-bit-secret-key-here
app.jwt.expiration=900000
```

### Run

```bash
./mvnw spring-boot:run
```

The server starts at `http://localhost:8080`.

### Quick API Test

```bash
# Register
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Aman","email":"aman@test.com","password":"pass123"}'

# Login
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"email":"aman@test.com","password":"pass123"}'

# Use the returned token for protected routes
curl http://localhost:8080/api/movies \
  -H "Authorization: Bearer <your_token>"
```

---

## 10. Environment Variables

For production deployment (Render / Railway), set the following environment variables instead of hardcoding in `application.properties`:

| Variable | Description | Required |
|----------|-------------|----------|
| `DB_URL` | Full JDBC connection string | ✅ |
| `DB_USERNAME` | Database username | ✅ |
| `DB_PASSWORD` | Database password | ✅ |
| `JWT_SECRET` | 256-bit HMAC signing key | ✅ |

### Production `application.properties` pattern

```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
app.jwt.secret=${JWT_SECRET}
```

### Build & Start Commands (Render)

```bash
# Build
./mvnw clean install -DskipTests

# Start
java -jar target/BMSProject-0.0.1-SNAPSHOT.jar
```

---

<div align="center">

**© 2026 BookIt. All rights reserved.**

*Built with Spring Boot 4 · Java 21 · MySQL · JWT*

</div>
