package com.fleetsync.fleetsync.user;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * UserRepository — handles all SQL database operations for the "users" table.
 *
 * @Repository marks this class as a Spring-managed data access component.
 * Spring will automatically create an instance of this class and inject it
 * wherever it's needed (e.g. in UserService).
 *
 * We use JdbcClient (Spring 6+) instead of JPA/Hibernate to write plain SQL queries.
 * This gives us full control over the SQL and avoids the overhead of an ORM.
 */
@Repository
public class UserRepository {

    /**
     * JdbcClient is Spring's modern fluent API for executing SQL queries.
     * It's injected by Spring from the DataSource configured in application.properties.
     */
    private final JdbcClient jdbc;

    /**
     * Constructor injection — Spring automatically provides the JdbcClient bean.
     * Constructor injection is preferred over field injection because it makes
     * dependencies explicit and easier to test.
     */
    public UserRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Checks whether a user with the given email already exists in the database.
     * Used during registration to prevent duplicate email addresses.
     *
     * SQL: COUNT(*) returns 0 if no match, or a positive number if a match exists.
     * We compare > 0 to convert the count into a boolean.
     *
     * @param email the email address to check
     * @return true if the email is already registered, false otherwise
     */
    public boolean existsByEmail(String email) {
        return jdbc.sql("SELECT COUNT(*) FROM users WHERE email = :email")
                   .param("email", email)       // :email is a named parameter — prevents SQL injection
                   .query(Integer.class)         // map the result to an Integer
                   .single() > 0;               // .single() gets the one row returned by COUNT(*)
    }

    /**
     * Checks whether a user with the given username already exists in the database.
     * Used during registration to prevent duplicate usernames.
     *
     * @param username the username to check
     * @return true if the username is already taken, false otherwise
     */
    public boolean existsByUsername(String username) {
        return jdbc.sql("SELECT COUNT(*) FROM users WHERE username = :username")
                   .param("username", username)
                   .query(Integer.class)
                   .single() > 0;
    }

    /**
     * Inserts a new user record into the database.
     * Called by UserService after validating uniqueness and hashing the password.
     *
     * Note: created_at is not included — the database sets it automatically
     * via a DEFAULT CURRENT_TIMESTAMP constraint in schema.sql.
     *
     * @param username     the chosen username
     * @param email        the user's email address
     * @param passwordHash the BCrypt-hashed password (never the plain-text password)
     * @param role         the assigned role: "MANAGER" or "DRIVER"
     */
    public void save(String username, String email, String passwordHash, String role) {
        jdbc.sql("INSERT INTO users (username, email, password_hash, role) VALUES (:username, :email, :hash, :role)")
            .param("username", username)
            .param("email", email)
            .param("hash", passwordHash)   // stored as a BCrypt hash, e.g. "$2a$12$..."
            .param("role", role)
            .update();                     // .update() executes INSERT/UPDATE/DELETE statements
    }

    /**
     * Loads a user by their username — called by Spring Security during login.
     *
     * Returns an Optional<User> because the user might not exist.
     * The caller (UserService.loadUserByUsername) handles the empty case by
     * throwing UsernameNotFoundException.
     *
     * The lambda (rs, rowNum) -> new User(...) is a RowMapper — it converts each
     * database row (ResultSet rs) into a User record by reading column values.
     *
     * @param username the username to look up
     * @return an Optional containing the User if found, or empty if not found
     */
    public Optional<User> findByUsername(String username) {
        return jdbc.sql("SELECT * FROM users WHERE username = :username")
                   .param("username", username)
                   .query((rs, rowNum) -> new User(
                       rs.getLong("id"),                                    // read the "id" column as Long
                       rs.getString("username"),                            // read the "username" column
                       rs.getString("email"),                               // read the "email" column
                       rs.getString("password_hash"),                       // read the hashed password
                       rs.getString("role"),                                // read the role (MANAGER/DRIVER)
                       rs.getTimestamp("created_at").toLocalDateTime()      // convert SQL Timestamp → LocalDateTime
                   ))
                   .optional();  // returns Optional.empty() if no row matched
    }
}
