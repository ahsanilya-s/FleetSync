package com.fleetsync.fleetsync.user;

import jakarta.validation.constraints.*;

/**
 * RegisterRequest — represents the JSON body the client sends to POST /api/auth/register.
 *
 * This is a Java record used as a Data Transfer Object (DTO).
 * Spring automatically deserializes the incoming JSON into this record.
 *
 * The validation annotations (from jakarta.validation) are checked by @Valid in the controller.
 * If any field fails validation, Spring returns a 400 Bad Request before the service is called.
 *
 * Example JSON body:
 * {
 *   "username": "john_doe",
 *   "email": "john@example.com",
 *   "password": "securePass1",
 *   "role": "DRIVER"
 * }
 */
public record RegisterRequest(

    /**
     * The desired login username.
     * @NotBlank  — must not be null or empty string
     * @Size      — must be between 3 and 50 characters
     * @Pattern   — only letters, numbers, and underscores allowed (no spaces or special chars)
     */
    @NotBlank
    @Size(min = 3, max = 50)
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username may only contain letters, numbers, and underscores")
    String username,

    /**
     * The user's email address.
     * @NotBlank — must not be null or empty
     * @Email    — must be a valid email format (e.g. user@domain.com)
     */
    @NotBlank
    @Email
    String email,

    /**
     * The plain-text password chosen by the user.
     * This is hashed with BCrypt in UserService before being stored — never saved as-is.
     * @NotBlank — must not be null or empty
     * @Size     — must be between 8 and 128 characters
     */
    @NotBlank
    @Size(min = 8, max = 128)
    String password,

    /**
     * The role to assign to this user on registration.
     * Accepted values: "MANAGER" or "DRIVER" (case-insensitive — normalized to uppercase in UserService)
     * @NotBlank — must not be null or empty
     * @Pattern  — enforces only MANAGER or DRIVER are accepted
     */
    @NotBlank
    @Pattern(regexp = "^(MANAGER|DRIVER)$", message = "Role must be MANAGER or DRIVER")
    String role
) {}
