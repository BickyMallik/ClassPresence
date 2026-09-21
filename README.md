# ClassPresence — Backend

> Smart Attendance Management System — REST API built with Spring Boot

[![Java](https://img.shields.io/badge/Java-21-orange)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)](https://www.postgresql.org/)

---

## Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Features](#features)
- [Architecture](#architecture)
- [API Endpoints](#api-endpoints)
- [Setup & Installation](#setup--installation)
- [Environment Variables](#environment-variables)
- [Database Schema](#database-schema)
- [Security](#security)
- [Author](#author)

---

## Overview

ClassPresence is a full-stack attendance management system designed for colleges. This repository contains the **Spring Boot backend** that powers the REST API.

Students mark attendance using a session-based OTP shared by the teacher. The system detects proxy attendance using device fingerprinting and GPS geofencing, and sends automated email notifications for registration, approval, OTP delivery, and password resets.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.x |
| Security | Spring Security + JWT |
| ORM | Spring Data JPA + Hibernate |
| Database | PostgreSQL |
| Email | JavaMailSender (Gmail SMTP) |
| Build Tool | Maven |
| Password Hashing | BCrypt |

---

## Features

- **JWT Authentication** — stateless, role-based (TEACHER / STUDENT)
- **Student Self-Registration** — with teacher approval workflow (PENDING → APPROVED / REJECTED)
- **OTP-Based Attendance** — session-scoped 6-digit OTP with 10-minute expiry
- **Async Email Notifications** — OTP delivery, approval/rejection, low attendance warning
- **Proxy Detection** — device fingerprint matching + rapid submission burst detection
- **GPS Geofencing** — Haversine formula to validate student is within 100m of teacher
- **Forgot Password Flow** — UUID token with 15-minute expiry, email reset link
- **PDF Attendance Reports** — downloadable per subject
- **Weekly Attendance Analytics** — week-wise session and attendance breakdown
- **Department Management** — pre-seeded department list for structured registration

---

## Architecture

```
src/main/java/com/AttendPulse/attend_backend/
│
├── controller/         # REST controllers (AuthController, TeacherController, StudentController)
├── service/            # Business logic (AuthService, TeacherService, StudentService, EmailService)
├── entity/             # JPA entities (User, Student, Subject, AttendanceSession, AttendanceRecord...)
├── repository/         # Spring Data JPA repositories
├── dto/                # Request/Response DTOs
├── security/           # JWT filter, JWT util, Security config, UserDetailsService
└── AttendBackendApplication.java
```

---

## API Endpoints

### Auth — `/api/auth`

| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/register` | Public | Register a teacher |
| POST | `/login` | Public | Login (teacher or student) |
| POST | `/student/register` | Public | Student self-registration |
| GET | `/me` | Authenticated | Get current user profile |
| GET | `/departments` | Public | List all departments |
| POST | `/forgot-password` | Public | Send password reset email |
| POST | `/reset-password` | Public | Reset password using token |

### Teacher — `/api/teacher`

| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/subject` | TEACHER | Add a new subject |
| GET | `/subjects` | TEACHER | Get teacher's subjects |
| POST | `/student` | TEACHER | Add a student directly |
| GET | `/students/all` | TEACHER | Get all students |
| GET | `/students/pending` | TEACHER | Get pending approval students |
| PUT | `/students/approve/{userId}` | TEACHER | Approve a student |
| PUT | `/students/reject/{userId}` | TEACHER | Reject a student |
| POST | `/enroll/{studentId}/{subjectId}` | TEACHER | Enroll student in subject |
| POST | `/session/start` | TEACHER | Start attendance session with OTP |
| PUT | `/session/lock/{sessionId}` | TEACHER | Lock a session |
| GET | `/attendance/weekly/{subjectId}` | TEACHER | Week-wise attendance data |
| GET | `/report/pdf/{subjectId}` | TEACHER | Download PDF attendance report |
| GET | `/flagged` | TEACHER | Get proxy-flagged attendance records |

### Student — `/api/student`

| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/attendance/mark` | STUDENT | Mark attendance with OTP |
| GET | `/attendance/overall` | STUDENT | Get overall attendance percentage |
| GET | `/attendance/subject/{subjectId}` | STUDENT | Get subject-wise attendance |

---

## Setup & Installation

### Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL 14+
- Gmail account (for SMTP — see Environment Variables)

### Steps

**1. Clone the repository**
```bash
git clone https://github.com/BickyMallik/ClassPresence.git
cd AttendDigital
```

**2. Create PostgreSQL database**
```sql
CREATE DATABASE attendx_db;
```

**3. Seed departments**
```sql
INSERT INTO departments (name) VALUES
('Computer Science Engineering'),
('Information Technology'),
('Electronics and Communication Engineering'),
('Electrical Engineering'),
('Civil Engineering'),
('Mechanical Engineering'),
('Computer Science Engineering - Artificial Intelligence'),
('Computer Science Engineering - Cyber Security'),
('Computer Science Engineering - Data Science');
```

**4. Configure environment variables**

Create `src/main/resources/application.properties` (see [Environment Variables](#environment-variables))

**5. Run the application**
```bash
mvn spring-boot:run
```

Backend runs on `http://localhost:8080`

---

## Environment Variables

Configure these in `application.properties`:

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/classpresence_db
spring.datasource.username=YOUR_DB_USERNAME
spring.datasource.password=YOUR_DB_PASSWORD

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# JWT
jwt.secret=YOUR_JWT_SECRET_KEY

# Gmail SMTP
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=YOUR_GMAIL_ADDRESS
spring.mail.password=YOUR_GMAIL_APP_PASSWORD
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

> **Note:** For Gmail SMTP, use an **App Password** (not your regular Gmail password). Enable 2FA on your Google account first, then generate an App Password from Google Account → Security → App Passwords.

---

## Database Schema

**Core Tables:**

| Table | Description |
|---|---|
| `users` | Teachers and students with role and status |
| `students` | Student-specific data (roll number, batch, department) |
| `departments` | Department master list |
| `subjects` | Subjects created by teachers |
| `enrollments` | Student-subject enrollment mapping |
| `attendance_sessions` | OTP sessions with geolocation and expiry |
| `attendance_records` | Individual attendance marks with proxy flag |
| `password_reset_tokens` | Tokens for forgot password flow |

---

## Security

- All endpoints except auth routes require a valid **JWT Bearer token**
- Passwords hashed with **BCrypt**
- Stateless session management (no server-side sessions)
- CORS configured for frontend origin (`http://localhost:5173`)
- Role-based access control — `TEACHER` and `STUDENT` roles enforced at endpoint level
- Proxy detection via **device fingerprint** matching per session
- **GPS Geofencing** — Haversine formula rejects attendance if student is >100m from teacher

---

## Author

**Bicky Mallik**
- GitHub: [@BickyMallik](https://github.com/BickyMallik)
- LinkedIn: [linkedin.com/in/bicky-mallik](https://linkedin.com/in/bicky-mallik)
- B.Tech CSE — Budge Budge Institute of Technology (2023–2027)
