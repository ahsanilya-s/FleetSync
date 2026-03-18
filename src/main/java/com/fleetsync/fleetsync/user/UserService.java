package com.fleetsync.fleetsync.user;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository repo;
    private final BCryptPasswordEncoder encoder;

    public UserService(UserRepository repo, BCryptPasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    public void register(RegisterRequest req) {
        if (repo.existsByEmail(req.email()))
            throw new IllegalArgumentException("Email already in use");

        if (repo.existsByUsername(req.username()))
            throw new IllegalArgumentException("Username already taken");

        repo.save(req.username(), req.email(), encoder.encode(req.password()));
    }
}
