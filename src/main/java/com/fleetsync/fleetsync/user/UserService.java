package com.fleetsync.fleetsync.user;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository repo;
    private final BCryptPasswordEncoder encoder;

    public UserService(UserRepository repo, BCryptPasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    // Registers a new user after validating uniqueness of email and username
    public void register(RegisterRequest req) {
        // Reject if email is already associated with another account
        if (repo.existsByEmail(req.email()))
            throw new IllegalArgumentException("Email already in use");

        // Reject if username is already taken
        if (repo.existsByUsername(req.username()))
            throw new IllegalArgumentException("Username already taken");

        // Validate that the provided role is one of the allowed values
        String role = req.role().toUpperCase();
        if (!role.equals("MANAGER") && !role.equals("DRIVER"))
            throw new IllegalArgumentException("Role must be MANAGER or DRIVER");

        // Hash the plain-text password before storing it
        repo.save(req.username(), req.email(), encoder.encode(req.password()), role);
    }

    // Called by Spring Security to load user credentials during login (HTTP Basic)
    // Throws UsernameNotFoundException if no user exists with the given username
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return repo.findByUsername(username)
            .map(user -> new org.springframework.security.core.userdetails.User(
                user.username(),
                user.passwordHash(),
                // Grant authority as ROLE_<role> e.g. ROLE_MANAGER, ROLE_DRIVER
                List.of(new SimpleGrantedAuthority("ROLE_" + user.role()))
            ))
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}
