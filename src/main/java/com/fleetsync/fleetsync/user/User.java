package com.fleetsync.fleetsync.user;

import java.time.LocalDateTime;

public record User(
    Long id,
    String username,
    String email,
    String passwordHash,
    String role,           // Role of the user e.g. MANAGER, DRIVER
    LocalDateTime createdAt
) {}
