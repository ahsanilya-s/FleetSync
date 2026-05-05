package com.fleetsync.fleetsync.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class DriverRequestDto {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "License number is required")
    private String licenseNumber;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    private LocalDate licenseExpiry;

    @NotNull(message = "User ID is required")
    private Long userId;
}
