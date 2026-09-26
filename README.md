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
7. [Authentication Deep Dive — Access & Refresh Tokens](#7-authentication-deep-dive--access--refresh-tokens)
8. [API Reference](#8-api-reference)
9. [Error Handling](#9-error-handling)
10. [Getting Started](#10-getting-started)
11. [Environment Variables](#11-environment-variables)

---

## 1. Project Overview

**BookIt** is a production-grade movie ticket booking platform built on **Spring Boot 4** with a stateless JWT-based security model, using a **short-lived access token + long-lived httpOnly-cookie refresh token** pair so sessions stay alive without ever exposing a long-lived credential to client-side JavaScript. It provides a clean RESTful API layer for browsing movies, selecting seats, booking tickets, and managing the full cinema ecosystem through a role-separated admin interface.

### Core Capabilities

| Area | Description |
|------|-------------|
| **Authentication** | Stateless JWT auth — register, login, silent access-token refresh via httpOnly cookie, server-side logout |
| **Movie Management** | Full CRUD — browse, search, filter by genre and language |
| **Booking Engine** | Real-time seat selection, booking creation, and cancellation with server-side seat-conflict checks |
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
│  → validates Bearer access token on every request    │
│  → fails OPEN (not throw) on expired/invalid token,   │
│    letting the entry point return a clean 401         │
│  → populates SecurityContextHolder                   │
│                                                      │
│  SecurityConfig  ·  JwtUtil  ·  JwtAuthEntryPoint    │
│  CookieUtil  ·  CustomUserDetailsService              │
│  PasswordConfig                                      │
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
       → extracts Bearer access token from Authorization header
       → JwtUtil validates signature + expiry
       → on success: loads UserDetails, sets Authentication in SecurityContext
       → on failure (expired/malformed/missing): clears SecurityContext and
         lets the request continue unauthenticated — never throws past the
         filter, so the authorization stage below produces a clean 401
         instead of a generic 500
[3]  SecurityConfig authorization rules evaluated
       → PUBLIC / USER / ADMIN endpoint matching
       → unauthenticated + protected route → JwtAuthEntryPoint returns 401
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
│       │       │   └── UserController.java          # login, register, refresh-token, logout
│       │       │
│       │       ├── dto/                             # Data Transfer Objects
│       │       │   ├── AuthResponse.java            # JWT access token + user info on login
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
│       │       │   │   ├── JwtAuthenticationFilter.java   # OncePerRequestFilter — fails open on bad tokens
│       │       │   │   ├── JwtAuthEntryPoint.java         # 401 handler
│       │       │   │   └── JwtUtil.java                   # access + refresh token generation & validation
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
│       │       │   └── UserService.java             # login, refreshToken, logout — cookie lifecycle
│       │       │
│       │       ├── util/
│       │       │   └── CookieUtil.java               # sets/reads/clears the httpOnly refresh cookie
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
  → JWT access token generated via JwtUtil
  → AuthResponse { token, user } returned

POST /api/users/login
  → LoginRequest { email, password }
  → AuthenticationManager.authenticate()
  → CustomUserDetailsService.loadUserByUsername()
  → BCrypt match verified
  → JWT access token generated
  → JWT refresh token generated and set as an httpOnly cookie via CookieUtil
  → AuthResponse { token, user } returned in the body (refresh token never
    appears in the JSON body or localStorage — only the cookie)

Subsequent protected requests:
  → Authorization: Bearer <access_token> header
  → JwtAuthenticationFilter.doFilterInternal()
      → JwtUtil.extractUsername() / validateAccessToken()
      → on expired/invalid: SecurityContext cleared, request proceeds
        unauthenticated (does NOT throw) → JwtAuthEntryPoint returns 401
      → on valid: UsernamePasswordAuthenticationToken set in SecurityContext
  → Controller executes

POST /api/users/refresh-token   (called automatically by the client on a 401)
  → reads the httpOnly refresh cookie via CookieUtil
  → JwtUtil validates the refresh token (separate signing key + expiry
    from the access token)
  → issues a new access token (and rotates the refresh cookie)
  → { accessToken } returned in the JSON body

POST /api/users/logout
  → CookieUtil clears the refresh cookie server-side (Max-Age=0)
  → this is the ONLY way the refresh cookie is invalidated — since it's
    httpOnly, client-side JavaScript can never read or delete it directly
```

### Security Components

| Class | Package | Responsibility |
|-------|---------|----------------|
| `SecurityConfig` | `config` | Filter chain, CORS, CSRF, session policy, authorization rules |
| `JwtAuthenticationFilter` | `security/jwt` | `OncePerRequestFilter` — intercepts every request, validates the access token, **fails open (never throws)** on an expired/invalid token so the request falls through to a clean 401 instead of a raw 500 |
| `JwtUtil` | `security/jwt` | Generates and validates **both** access and refresh tokens, each signed with its own secret and its own expiry |
| `JwtAuthEntryPoint` | `security/jwt` | Returns `401 Unauthorized` JSON on unauthenticated access |
| `CookieUtil` | `util` | Sets, reads, and clears the refresh token as an `httpOnly` cookie — the single place that owns the cookie's lifecycle |
| `CustomUserDetailsService` | `security/service` | Implements `UserDetailsService` — loads `User` entity by email |
| `PasswordConfig` | `config` | Defines `BCryptPasswordEncoder` bean |

### Role-Based Access Control

| Role | Scope |
|------|-------|
| `PUBLIC` | `/api/users/register`, `/api/users/login`, `/api/users/refresh-token`, `/api/users/logout`, all `GET` on movies / shows / theaters / cities |
| `USER` | All `PUBLIC` + create booking, view own bookings, cancel own bookings |
| `ADMIN` | All endpoints — full CRUD on movies, theaters, screens, seats, shows + all user and booking management |

> Note: `refresh-token` and `logout` must stay reachable without a *valid* access token, since the whole point of calling them is that the access token has just expired. `SecurityConfig` allows them through `permitAll()`; the JWT filter still runs on these requests (it runs on every request, permitAll or not) but its fail-open behavior means an expired access token attached to these calls never blocks them from reaching the controller.

### Security Configuration Summary

| Setting | Value |
|---------|-------|
| Session Management | `STATELESS` — no server-side session |
| CSRF | Disabled (stateless JWT API) |
| Password Encoding | BCrypt |
| Token Algorithm | HS256 (HMAC-SHA256) |
| Access Token Expiry | Short-lived (configurable, e.g. 15 min in production) |
| Refresh Token Expiry | 30 days |
| Refresh Token Storage | `httpOnly` cookie, `Path=/api` — never exposed to JavaScript, never stored in `localStorage` |
| Cookie `SameSite` / `Secure` (local dev) | `Strict` / `false` — safe because `localhost:<port>` origins are treated as same-site by browsers regardless of port |
| Cookie `SameSite` / `Secure` (production) | `None` / `true` — **required** once frontend and backend are on genuinely different origins (e.g. Vercel + Render); `SameSite=None` cookies are only ever sent by browsers when `Secure` is also set |
| CORS | Configured via `CorsConfig` / `SecurityConfig` for the deployed frontend origin(s), with `allowCredentials(true)` so the refresh cookie is actually sent cross-origin |

---

## 7. Authentication Deep Dive — Access & Refresh Tokens

This section documents the reasoning behind the auth design, since it's easy to get subtly wrong.

### Why two tokens instead of one

A single long-lived JWT is simple but risky: if it's ever exposed (XSS, a logged request, a browser extension reading `localStorage`), an attacker has a long-lived, unrevocable credential. The two-token pattern splits the trade-off:

- **Access token** — short-lived, sent as a `Bearer` header on every request, stored in `localStorage`/memory on the client. If it leaks, the damage window is small because it expires quickly.
- **Refresh token** — long-lived, but stored in an `httpOnly` cookie the browser manages automatically. JavaScript can never read, copy, or exfiltrate it. Only the server can issue or revoke it.

### The single-source-of-truth principle

The most important lesson from building this: **exactly one thing should ever decide "is this session still valid."** Early in development, multiple independent pieces of logic — a request filter, a state-hydration function, a periodic client-side timer, a route guard — each made their own judgment about token expiry. Because they disagreed with each other under timing edge cases, sessions could be invalidated even when a valid refresh should have kept them alive. The fix was architectural, not a patch: the backend's `JwtAuthenticationFilter` never makes an authorization *decision* on a bad token, it just fails open and lets Spring Security's normal authorization stage produce a consistent 401 — and on the client, a single bootstrap routine is the only thing allowed to decide whether a session should be refreshed or ended.

### Failure-mode handling

| Scenario | Backend behavior |
|----------|------------------|
| Access token expired, refresh cookie valid | `JwtAuthenticationFilter` fails open → client gets 401 → client calls `/refresh-token` → new access token issued |
| Access token expired, refresh cookie also expired/missing | `/refresh-token` throws, controller returns 401 with a message → client clears local session state |
| Access token malformed/tampered | Same fail-open path as expired — treated identically, never a 500 |
| Logout | Refresh cookie cleared server-side via `Set-Cookie; Max-Age=0` — this is the only valid way to end a session early, since the cookie is inaccessible to JavaScript |

### Cross-origin deployment note

Locally, a frontend on `localhost:5173` calling a backend on `localhost:8080` are different **origins** but the same **site** (browsers ignore port when determining "site"), so `SameSite=Strict` cookies are still sent. Once deployed with the frontend on Vercel and the backend on Render, they are genuinely different sites — `SameSite=Strict` (and even `Lax`) cookies would silently stop being sent on cross-origin requests. This must be switched to `SameSite=None; Secure=true` for production, gated behind an environment-specific config so local development isn't forced onto HTTPS.

---

## 8. API Reference

### Base URL

```
http://localhost:8080/api
```

### Authentication

All protected endpoints require the following header:

```
Authorization: Bearer <jwt_access_token>
```

The refresh token is never sent manually — it travels automatically as an `httpOnly` cookie on requests to `/api/users/refresh-token` and `/api/users/logout`, as long as the client sends the request with credentials included (e.g. `withCredentials: true` in Axios).

---

### User & Auth Endpoints `/api/users`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/api/users/register` | PUBLIC | Register new user, returns access token |
| `POST` | `/api/users/login` | PUBLIC | Authenticate; returns access token in body, sets refresh token as httpOnly cookie |
| `POST` | `/api/users/refresh-token` | PUBLIC | Reads the refresh cookie, returns a new access token |
| `POST` | `/api/users/logout` | PUBLIC | Clears the refresh cookie server-side |
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
  "user": {
    "id": 1,
    "name": "Aman Kumar",
    "email": "aman@example.com",
    "role": "USER"
  }
}
```

**Refresh-token response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "email": "aman@example.com"
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
| `GET` | `/api/bookings` | ADMIN | Get all bookings (admin dashboard) |

**Booking request:**
```json
{
  "userId": 1,
  "showId": 5,
  "seatIds": [12, 13, 14]
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
| UserController | 6 | 4 | 0 | 2 |
| MovieController | 8 | 5 | 0 | 3 |
| TheaterController | 6 | 3 | 0 | 3 |
| ScreenController | 6 | 3 | 0 | 3 |
| SeatController | 5 | 2 | 0 | 3 |
| ShowController | 8 | 5 | 0 | 3 |
| BookingController | 6 | 1 | 4 | 1 |
| CityController | 5 | 2 | 0 | 3 |
| **Total** | **50** | **25** | **4** | **21** |

---

## 9. Error Handling

All exceptions are handled centrally by `GlobalExceptionHandler` (`@RestControllerAdvice`). Clients always receive a consistent JSON error response.

### Exception Types

| Exception Class | HTTP Status | Trigger Scenario |
|-----------------|-------------|-----------------|
| `ResourceNotFoundException` | `404 Not Found` | Entity not found by ID |
| `DuplicateResourceException` | `409 Conflict` | Duplicate email, title, etc. |
| `BookingException` | `400 Bad Request` | Seat already booked, invalid booking state |
| `InvalidCredentialsException` | `401 Unauthorized` | Wrong email or password on login |
| `JwtAuthEntryPoint` (security) | `401 Unauthorized` | Missing, invalid, or expired access token — **never** surfaced as a 500, by design (see [Section 7](#7-authentication-deep-dive--access--refresh-tokens)) |

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

## 10. Getting Started

### Prerequisites

- Java 21
- Maven 3.9+
- MySQL 8.x (local or Aiven Cloud)

### Clone & Build

```bash
git clone https://github.com/TechFourgeBuild/BmsBackend.git
cd BmsBackend

# Build (skip tests for quick start)
./mvnw clean install -DskipTests
```

### Configure Environment

Create `src/main/resources/application.properties` or set environment variables (see [Section 11](#11-environment-variables)).

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/bookit_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=your_db_user
spring.datasource.password=your_db_password

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# JWT — access token
jwt.secret=your-256-bit-secret-key-here
jwt.expiration=900000

# JWT — refresh token (separate secret, longer expiry)
jwt.secret.refreshToken=a-different-256-bit-secret-key-here
jwt.refresh.expiration=2592000000
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

# Login (-c saves the refresh cookie to a file for the next step)
curl -c cookies.txt -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"email":"aman@test.com","password":"pass123"}'

# Use the returned access token for protected routes
curl http://localhost:8080/api/movies \
  -H "Authorization: Bearer <your_access_token>"

# Silently refresh the access token using the saved cookie
curl -b cookies.txt -X POST http://localhost:8080/api/users/refresh-token
```

---

## 11. Environment Variables

For production deployment (Render / Railway), set the following environment variables instead of hardcoding in `application.properties`:

| Variable | Description | Required |
|----------|-------------|----------|
| `DB_URL` | Full JDBC connection string | ✅ |
| `DB_USERNAME` | Database username | ✅ |
| `DB_PASSWORD` | Database password | ✅ |
| `JWT_SECRET` | 256-bit HMAC signing key for access tokens | ✅ |
| `JWT_REFRESH_SECRET` | 256-bit HMAC signing key for refresh tokens — **must differ** from `JWT_SECRET` | ✅ |
| `COOKIE_SECURE` | `true` in production (required for `SameSite=None`); `false` for local HTTP dev | ✅ |

### Production `application.properties` pattern

```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
jwt.secret=${JWT_SECRET}
jwt.secret.refreshToken=${JWT_REFRESH_SECRET}
```

> **Deployment note:** when the frontend and backend are on different domains (e.g. a Vercel frontend and a Render backend), `CookieUtil` must set the refresh cookie with `SameSite=None` and `Secure=true`, and `SecurityConfig`'s CORS configuration must list the frontend's exact deployed origin with `allowCredentials(true)` — otherwise the browser will silently refuse to send or accept the refresh cookie cross-origin, and login will appear to fail even with correct credentials.

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

**Repo:** [github.com/TechFourgeBuild/BmsBackend](https://github.com/TechFourgeBuild/BmsBackend.git)

</div>
