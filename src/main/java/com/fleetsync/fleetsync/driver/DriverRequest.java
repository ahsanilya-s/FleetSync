package com.fleetsync.fleetsync.driver;

import jakarta.validation.constraints.*;

// Represents the JSON body sent by the manager when registering a new driver
// Validation annotations ensure all required fields are present and correctly formatted
public record DriverRequest(

    @NotBlank
    @Size(max = 100)
    String fullName,

    // Unique license number issued by the transport authority
    @NotBlank
    @Size(max = 50)
    String licenseNumber,

    @NotBlank
    @Size(max = 20)
    String phone,

    @NotBlank
    @Email
    String email
) {}
