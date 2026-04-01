package com.fleetsync.fleetsync.user;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * UserService — handles user registration and authentication logic.
 *
 * @Service marks this class as a Spring-managed service component.
 * Spring creates one instance of this class and injects it wherever needed.
 *
 * This class implements UserDetailsService — a Spring Security interface that
 * requires us to implement loadUserByUsername(). Spring Security calls this method
 * automatically during every login attempt to fetch the user from the database.
 */
@Service
public class UserService implements UserDetailsService {

    /** Handles all database operations for the users table. */
    private final UserRepository repo;

    /**
     * BCryptPasswordEncoder is used to hash plain-text passwords before storing them.
     * It's also used by Spring Security (via DaoAuthenticationProvider) to verify
     * passwords during login — it hashes the incoming password and compares it to the stored hash.
     */
    private final BCryptPasswordEncoder encoder;

    /**
     * Constructor injection — Spring provides both dependencies automatically.
     *
     * @param repo    the UserRepository bean
     * @param encoder the BCryptPasswordEncoder bean (defined in SecurityConfig)
     */
    public UserService(UserRepository repo, BCryptPasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    /**
     * Registers a new user account after validating uniqueness and hashing the password.
     *
     * Steps:
     *   1. Check if the email is already in use → throw 409 if so
     *   2. Check if the username is already taken → throw 409 if so
     *   3. Validate the role is MANAGER or DRIVER → throw 400 if invalid
     *   4. Hash the plain-text password with BCrypt
     *   5. Save the new user to the database
     *
     * IllegalArgumentException is caught by UserController's @ExceptionHandler
     * and converted to the appropriate HTTP error response.
     *
     * @param req the registration request containing username, email, password, and role
     * @throws IllegalArgumentException if email/username is taken or role is invalid
     */
    public void register(RegisterRequest req) {
        // Step 1: Reject if this email is already associated with another account
        if (repo.existsByEmail(req.email()))
            throw new IllegalArgumentException("Email already in use");

        // Step 2: Reject if this username is already taken by another user
        if (repo.existsByUsername(req.username()))
            throw new IllegalArgumentException("Username already taken");

        // Step 3: Normalize the role to uppercase and validate it
        // req.role() could be "manager" or "MANAGER" — we accept both
        String role = req.role().toUpperCase();
        if (!role.equals("MANAGER") && !role.equals("DRIVER"))
            throw new IllegalArgumentException("Role must be MANAGER or DRIVER");

        // Step 4 & 5: Hash the password and save the user
        // encoder.encode() applies BCrypt hashing — the result looks like "$2a$12$..."
        repo.save(req.username(), req.email(), encoder.encode(req.password()), role);
    }

    /**
     * Called automatically by Spring Security during every login attempt.
     *
     * When a client sends an HTTP Basic Authorization header, Spring Security:
     *   1. Decodes the Base64 credentials to get username and password
     *   2. Calls this method to load the user from the database
     *   3. Uses BCrypt to compare the incoming password with the stored hash
     *   4. Grants or denies access based on the result
     *
     * We return Spring's built-in User class (not our User record) because
     * Spring Security requires a UserDetails object, not our custom record.
     *
     * @param username the username extracted from the Authorization header
     * @return a UserDetails object containing the username, hashed password, and authorities
     * @throws UsernameNotFoundException if no user exists with the given username
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return repo.findByUsername(username)
            .map(user -> new org.springframework.security.core.userdetails.User(
                user.username(),       // the username Spring Security will use
                user.passwordHash(),   // the BCrypt hash — Spring Security verifies the incoming password against this
                // Authorities define what the user is allowed to do.
                // Spring Security expects roles prefixed with "ROLE_" (e.g. "ROLE_MANAGER").
                // hasRole("MANAGER") in SecurityConfig checks for "ROLE_MANAGER" automatically.
                List.of(new SimpleGrantedAuthority("ROLE_" + user.role()))
            ))
            // If no user was found, throw this exception — Spring Security converts it to 401 Unauthorized
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}
