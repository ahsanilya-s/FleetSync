# User Registration Feature — Beginner's Guide
**Project:** FleetSync  
**Feature:** User Registration (Username, Email, Password)  
**Stack:** React + Vite (Frontend) · Spring Boot / Java 21 (Backend) · MySQL (Database)

---

## 1. What Are We Building?

We want users to be able to **create an account** in FleetSync.

The flow is simple:
1. User opens a form in the browser and types their **username**, **email**, and **password**
2. The form sends that data to our **Spring Boot backend** (the server)
3. The server **checks** the data, **hashes** the password (so it's never stored as plain text), and **saves** the user to the **MySQL database**

Think of it like signing up for any website — but we're building it ourselves from scratch.

---

## 2. Why Do We Need Security?

Before writing any code, let's understand *why* we do certain things:

| What We Do | Why |
|---|---|
| Hash the password with BCrypt | If the database is ever hacked, passwords are unreadable |
| Validate input on the server | The frontend can be bypassed — the server must always double-check |
| Use parameterized SQL queries | Prevents hackers from injecting malicious SQL |
| Unique constraint on email/username | Prevents two people from registering with the same email |
| Don't log the password | Logs are often readable by many people — passwords must never appear there |

---

## 3. The Database — Where Data Lives

### What is a database table?
Think of it like an Excel spreadsheet. Each **row** is one user, each **column** is a piece of info.

### 3.1 Create the `users` Table (MySQL)

```sql
CREATE TABLE users (
    id            BIGINT       AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

> **Note:** MySQL uses `BIGINT AUTO_INCREMENT` instead of PostgreSQL's `BIGSERIAL`. The result is the same — an auto-incrementing ID.

### What each column means:
- `id` — a unique number automatically assigned to each user (1, 2, 3, ...)
- `username` — the user's chosen name, must be unique
- `email` — their email, must be unique
- `password_hash` — the **hashed** (scrambled) version of their password, never the real one
- `created_at` — the date/time they registered, set automatically by MySQL

---

## 4. Backend — Spring Boot (The Server)

### What is the backend?
The backend is the **server-side code** that runs on your computer (or a server). It receives requests from the frontend, processes them, and talks to the database.

---

### 4.1 Add Dependencies to `pom.xml`

Dependencies are **libraries** (pre-written code) that we add to our project so we don't have to write everything from scratch.

Open `pom.xml` and add these inside `<dependencies>`:

```xml
<!-- Spring Security — we use it ONLY for BCrypt password hashing -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- Validation — lets us use @NotBlank, @Email, etc. on our DTO -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>

<!-- MySQL Driver — lets Java talk to MySQL -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```

> We're NOT using Spring Security for login/logout yet — just borrowing its BCrypt tool.

---

### 4.2 Configure the Database in `application.properties`

This file tells Spring Boot **how to connect** to your MySQL database.

```properties
# Replace fleetsync with your database name, and fill in your MySQL username/password
spring.datasource.url=jdbc:mysql://localhost:3306/fleetsync
spring.datasource.username=<your_mysql_username>
spring.datasource.password=<your_mysql_password>
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Disable Spring Security's auto-login page (we only use BCrypt from it)
spring.security.user.name=disabled

# CORS — allow our React frontend (running on port 5173) to call our API
spring.mvc.cors.allowed-origins=http://localhost:5173
spring.mvc.cors.allowed-methods=POST,GET,OPTIONS
spring.mvc.cors.allowed-headers=Content-Type
```

> **What is CORS?** Browsers block requests between different ports/domains by default. CORS tells the browser "yes, this frontend is allowed to talk to this backend."

---

### 4.3 Package Structure

Here's how we'll organize our Java files:

```
com.fleetsync.fleetsync/
├── user/
│   ├── RegisterRequest.java   ← the data coming FROM the frontend (DTO)
│   ├── User.java              ← represents a user row in the database
│   ├── UserRepository.java    ← talks to the database
│   ├── UserService.java       ← business logic (validation, hashing)
│   └── UserController.java    ← receives HTTP requests from the frontend
└── config/
    └── SecurityConfig.java    ← sets up the BCrypt password hasher
```

> **What is a DTO?** DTO = Data Transfer Object. It's just a simple class that holds the data sent from the frontend. We don't save it directly — we process it first.

---

### 4.4 `RegisterRequest.java` — What the Frontend Sends

```java
package com.fleetsync.fleetsync.user;

import jakarta.validation.constraints.*;

// A "record" is a simple Java class that just holds data (Java 16+)
public record RegisterRequest(

    @NotBlank                          // must not be empty
    @Size(min = 3, max = 50)           // between 3 and 50 characters
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Letters, numbers, underscores only")
    String username,

    @NotBlank
    @Email                             // must look like a valid email
    String email,

    @NotBlank
    @Size(min = 8, max = 128)          // password must be at least 8 characters
    String password
) {}
```

> Spring will automatically reject the request with a `400 Bad Request` if any of these rules fail — before our code even runs.

---

### 4.5 `User.java` — The Database Row as a Java Object

```java
package com.fleetsync.fleetsync.user;

import java.time.LocalDateTime;

// This represents one row in the "users" table
public record User(
    Long id,
    String username,
    String email,
    String passwordHash,
    LocalDateTime createdAt
) {}
```

---

### 4.6 `UserRepository.java` — Talking to the Database

```java
package com.fleetsync.fleetsync.user;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

// @Repository tells Spring: "this class talks to the database"
@Repository
public class UserRepository {

    private final JdbcClient jdbc;

    // Spring automatically gives us a JdbcClient — we just ask for it here
    public UserRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    // Returns true if someone already registered with this email
    public boolean existsByEmail(String email) {
        return jdbc.sql("SELECT COUNT(*) FROM users WHERE email = :email")
                   .param("email", email)
                   .query(Integer.class)
                   .single() > 0;
    }

    // Returns true if someone already registered with this username
    public boolean existsByUsername(String username) {
        return jdbc.sql("SELECT COUNT(*) FROM users WHERE username = :username")
                   .param("username", username)
                   .query(Integer.class)
                   .single() > 0;
    }

    // Inserts a new user row into the database
    public void save(String username, String email, String passwordHash) {
        jdbc.sql("INSERT INTO users (username, email, password_hash) VALUES (:username, :email, :hash)")
            .param("username", username)
            .param("email", email)
            .param("hash", passwordHash)
            .update();
    }
}
```

> **Why `:email` instead of `'` + email + `'`?** This is called a **parameterized query**. It prevents SQL injection — a common attack where hackers type SQL code into a form field to manipulate your database.

---

### 4.7 `UserService.java` — The Business Logic

```java
package com.fleetsync.fleetsync.user;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

// @Service tells Spring: "this class contains business logic"
@Service
public class UserService {

    private final UserRepository repo;
    private final BCryptPasswordEncoder encoder;

    public UserService(UserRepository repo, BCryptPasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    public void register(RegisterRequest req) {
        // Step 1: Check if email is already used
        if (repo.existsByEmail(req.email()))
            throw new IllegalArgumentException("Email already in use");

        // Step 2: Check if username is already taken
        if (repo.existsByUsername(req.username()))
            throw new IllegalArgumentException("Username already taken");

        // Step 3: Hash the password — NEVER store the real password
        // BCrypt turns "myPassword123" into something like "$2a$12$..."
        String hash = encoder.encode(req.password());

        // Step 4: Save to database
        repo.save(req.username(), req.email(), hash);
    }
}
```

---

### 4.8 `UserController.java` — The API Endpoint

```java
package com.fleetsync.fleetsync.user;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

// @RestController tells Spring: "this class handles HTTP requests"
@RestController
@RequestMapping("/api/auth")  // all endpoints here start with /api/auth
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    // POST /api/auth/register
    // @Valid triggers the validation rules we wrote in RegisterRequest
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)  // sends back "201 Created" on success
    public void register(@Valid @RequestBody RegisterRequest req) {
        service.register(req);
    }
}
```

**What happens for each scenario:**

| Situation | HTTP Response |
|---|---|
| Everything is valid, user created | `201 Created` |
| Missing field, bad email, short password | `400 Bad Request` |
| Email or username already exists | `409 Conflict` |
| Something crashed on the server | `500 Internal Server Error` |

---

### 4.9 `SecurityConfig.java` — Set Up BCrypt

```java
package com.fleetsync.fleetsync.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

// @Configuration tells Spring: "this class sets up app-wide settings"
@Configuration
public class SecurityConfig {

    // @Bean tells Spring to create this object and make it available everywhere
    // Cost factor 12 = how hard it is to crack (higher = slower = safer)
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
```

---

## 5. Frontend — React + Vite (The Browser UI)

### What is the frontend?
The frontend is what the user **sees and interacts with** in their browser. We use React (a JavaScript library) and Vite (a fast build tool).

---

### 5.1 Set Up the React Project

Run these commands in your terminal (only once):

```bash
npm create vite@latest fleetsync-ui -- --template react
cd fleetsync-ui
npm install
npm install axios react-hook-form
```

- `axios` — sends HTTP requests to our backend
- `react-hook-form` — manages form state and validation easily

---

### 5.2 `src/pages/Register.jsx` — The Registration Form

```jsx
import { useForm } from 'react-hook-form';
import axios from 'axios';

export default function Register() {
  // useForm gives us tools to manage the form
  const { register, handleSubmit, formState: { errors } } = useForm();

  // This runs when the user clicks "Register"
  const onSubmit = async (data) => {
    try {
      // Send the form data to our Spring Boot backend
      await axios.post('/api/auth/register', data);
      alert('Registration successful!');
    } catch (err) {
      // Show a helpful message based on what went wrong
      const msg = err.response?.status === 409
        ? 'Email or username already taken.'
        : 'Registration failed. Please try again.';
      alert(msg);
    }
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)}>

      <input
        placeholder="Username"
        {...register('username', {
          required: 'Required',
          minLength: { value: 3, message: 'Min 3 characters' },
          pattern: { value: /^[a-zA-Z0-9_]+$/, message: 'Letters, numbers, underscores only' }
        })}
      />
      {errors.username && <span>{errors.username.message}</span>}

      <input
        placeholder="Email"
        type="email"
        {...register('email', { required: 'Required' })}
      />
      {errors.email && <span>{errors.email.message}</span>}

      <input
        placeholder="Password"
        type="password"
        {...register('password', {
          required: 'Required',
          minLength: { value: 8, message: 'Min 8 characters' }
        })}
      />
      {errors.password && <span>{errors.password.message}</span>}

      <button type="submit">Register</button>
    </form>
  );
}
```

---

### 5.3 `vite.config.js` — Connect Frontend to Backend

```js
export default {
  server: {
    proxy: {
      '/api': 'http://localhost:8080'  // forward /api/* calls to Spring Boot
    }
  }
}
```

> Without this, the browser would block the request because the frontend runs on port `5173` and the backend on `8080`. The proxy makes them appear as one.

---

## 6. The Full Flow — Step by Step

```
User fills in the form (React)
        │
        ▼
react-hook-form checks the input (client-side)
        │
        ▼
POST /api/auth/register  { username, email, password }
        │  (goes through Vite proxy to Spring Boot)
        ▼
Spring Boot — @Valid checks RegisterRequest rules
        │
        ├─ Invalid? → 400 Bad Request (stops here)
        │
        ▼
UserService checks if email/username already exists in MySQL
        │
        ├─ Duplicate? → 409 Conflict (stops here)
        │
        ▼
BCrypt hashes the password  (e.g. "pass1234" → "$2a$12$...")
        │
        ▼
INSERT INTO users (username, email, password_hash) in MySQL
        │
        ▼
201 Created ← React shows "Registration successful!"
```

---

## 7. PostgreSQL vs MySQL — What Changed?

If you've seen this project use PostgreSQL before, here's exactly what's different with MySQL:

| Thing | PostgreSQL | MySQL |
|---|---|---|
| Maven dependency | `org.postgresql:postgresql` | `com.mysql:mysql-connector-j` |
| JDBC URL | `jdbc:postgresql://localhost:5432/fleetsync` | `jdbc:mysql://localhost:3306/fleetsync` |
| Driver class | `org.postgresql.Driver` | `com.mysql.cj.jdbc.Driver` |
| Auto-increment ID in SQL | `BIGSERIAL PRIMARY KEY` | `BIGINT AUTO_INCREMENT PRIMARY KEY` |
| Default timestamp in SQL | `DEFAULT NOW()` | `DEFAULT CURRENT_TIMESTAMP` |

Everything else — the Java code, the React code, the security logic — stays **exactly the same**.

---

## 8. Files to Create / Modify

| File | What to do | Why |
|---|---|---|
| `pom.xml` | Add 3 dependencies | Spring Security (BCrypt), Validation, MySQL driver |
| `application.properties` | Add DB config + CORS | Connect to MySQL, allow frontend requests |
| `src/main/resources/schema.sql` | Create `users` table | Store user data in MySQL |
| `user/RegisterRequest.java` | Create | Holds + validates data from frontend |
| `user/User.java` | Create | Represents a user in Java |
| `user/UserRepository.java` | Create | Reads/writes users in MySQL |
| `user/UserService.java` | Create | Checks duplicates, hashes password |
| `user/UserController.java` | Create | Handles `POST /api/auth/register` |
| `config/SecurityConfig.java` | Create | Provides the BCrypt bean |
| `fleetsync-ui/src/pages/Register.jsx` | Create | The registration form |
| `fleetsync-ui/vite.config.js` | Modify | Proxy `/api` to Spring Boot |

---

## 9. Test It Manually (No Frontend Needed)

Once the backend is running (`./mvnw spring-boot:run`), test with `curl`:

```bash
# Should return: 201 Created
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"ahsan","email":"ahsan@example.com","password":"securePass1"}'

# Should return: 409 Conflict (same email)
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"ahsan2","email":"ahsan@example.com","password":"securePass1"}'

# Should return: 400 Bad Request (password too short)
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"ahsan","email":"new@example.com","password":"123"}'
```

---

## 10. What's Next?

Once registration works, the natural next steps are:

- **Login** — `POST /api/auth/login` with JWT token response
- **Email verification** — send a confirmation link after registration
- **Roles** — assign `FLEET_MANAGER` or `DRIVER` role at registration
- **Password reset** — forgot password flow
