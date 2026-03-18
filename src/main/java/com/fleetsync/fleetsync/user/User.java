package com.fleetsync.fleetsync.user;

import java.time.LocalDateTime;

public record User(
    Long id,
    String username,
    String email,
    String passwordHash,
    LocalDateTime createdAt
) {}
