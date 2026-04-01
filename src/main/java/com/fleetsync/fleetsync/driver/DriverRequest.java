package com.fleetsync.fleetsync.driver;

import jakarta.validation.constraints.*;

/**
 * DriverRequest — represents the JSON body sent by the manager when registering a new driver.
 *
 * This is a DTO (Data Transfer Object) used to carry data from the HTTP request into the controller.
 * Spring deserializes the incoming JSON into this record automatically.
 *
 * Validation annotations are checked by @Valid in DriverController.
 * If any field fails, Spring returns 400 Bad Request before the controller method runs.
 *
 * Example JSON body:
 * {
 *   "fullName": "John Smith",
 *   "licenseNumber": "DL-2024-001",
 *   "phone": "+1234567890",
 *   "email": "john.smith@example.com"
 * }
 */
public record DriverRequest(

    /**
     * Full legal name of the driver.
     * @NotBlank — must not be null or empty
     * @Size     — max 100 characters
     */
    @NotBlank
    @Size(max = 100)
    String fullName,

    /**
     * Unique driving license number issued by the transport authority.
     * Uniqueness is enforced in DriverController to return a clear 409 error.
     * @NotBlank — must not be null or empty
     * @Size     — max 50 characters
     */
    @NotBlank
    @Size(max = 50)
    String licenseNumber,

    /**
     * Driver's contact phone number.
     * @NotBlank — must not be null or empty
     * @Size     — max 20 characters (accommodates international formats)
     */
    @NotBlank
    @Size(max = 20)
    String phone,

    /**
     * Driver's email address.
     * @NotBlank — must not be null or empty
     * @Email    — must be a valid email format (e.g. user@domain.com)
     */
    @NotBlank
    @Email
    String email
) {}
