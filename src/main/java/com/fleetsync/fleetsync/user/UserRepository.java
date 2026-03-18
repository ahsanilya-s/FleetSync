package com.fleetsync.fleetsync.user;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {

    private final JdbcClient jdbc;

    public UserRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public boolean existsByEmail(String email) {
        return jdbc.sql("SELECT COUNT(*) FROM users WHERE email = :email")
                   .param("email", email)
                   .query(Integer.class)
                   .single() > 0;
    }

    public boolean existsByUsername(String username) {
        return jdbc.sql("SELECT COUNT(*) FROM users WHERE username = :username")
                   .param("username", username)
                   .query(Integer.class)
                   .single() > 0;
    }

    public void save(String username, String email, String passwordHash) {
        jdbc.sql("INSERT INTO users (username, email, password_hash) VALUES (:username, :email, :hash)")
            .param("username", username)
            .param("email", email)
            .param("hash", passwordHash)
            .update();
    }
}
