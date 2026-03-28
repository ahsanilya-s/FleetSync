package com.fleetsync.fleetsync.user;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepository {

    private final JdbcClient jdbc;

    public UserRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    // Checks if a user with the given email already exists
    public boolean existsByEmail(String email) {
        return jdbc.sql("SELECT COUNT(*) FROM users WHERE email = :email")
                   .param("email", email)
                   .query(Integer.class)
                   .single() > 0;
    }

    // Checks if a user with the given username already exists
    public boolean existsByUsername(String username) {
        return jdbc.sql("SELECT COUNT(*) FROM users WHERE username = :username")
                   .param("username", username)
                   .query(Integer.class)
                   .single() > 0;
    }

    // Persists a new user with hashed password and assigned role
    public void save(String username, String email, String passwordHash, String role) {
        jdbc.sql("INSERT INTO users (username, email, password_hash, role) VALUES (:username, :email, :hash, :role)")
            .param("username", username)
            .param("email", email)
            .param("hash", passwordHash)
            .param("role", role)
            .update();
    }

    // Loads a user by username — used by Spring Security during authentication
    public Optional<User> findByUsername(String username) {
        return jdbc.sql("SELECT * FROM users WHERE username = :username")
                   .param("username", username)
                   .query((rs, rowNum) -> new User(
                       rs.getLong("id"),
                       rs.getString("username"),
                       rs.getString("email"),
                       rs.getString("password_hash"),
                       rs.getString("role"),
                       rs.getTimestamp("created_at").toLocalDateTime()
                   ))
                   .optional();
    }
}
