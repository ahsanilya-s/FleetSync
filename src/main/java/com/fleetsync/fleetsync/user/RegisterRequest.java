package com.fleetsync.fleetsync.user;

import jakarta.validation.constraints.*;

public record RegisterRequest(

    @NotBlank
    @Size(min = 3, max = 50)
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username may only contain letters, numbers, and underscores")
    String username,

    @NotBlank
    @Email
    String email,

    @NotBlank
    @Size(min = 8, max = 128)
    String password
) {};
