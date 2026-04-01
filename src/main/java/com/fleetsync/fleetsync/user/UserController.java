package com.fleetsync.fleetsync.user;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

/**
 * UserController — REST controller for user registration and profile endpoints.
 *
 * @RestController combines @Controller and @ResponseBody:
 *   - @Controller   → marks this as a Spring MVC controller (handles HTTP requests)
 *   - @ResponseBody → all return values are automatically serialized to JSON
 *
 * @RequestMapping("/api/auth") sets the base URL path for all methods in this class.
 * So a method mapped to "/register" will be reachable at "/api/auth/register".
 */
@RestController
@RequestMapping("/api/auth")
public class UserController {

    /**
     * UserService contains the business logic for registration and authentication.
     * It's injected by Spring via constructor injection.
     */
    private final UserService service;

    /**
     * Constructor injection — Spring automatically provides the UserService bean.
     * This is the recommended way to inject dependencies (over @Autowired on fields).
     */
    public UserController(UserService service) {
        this.service = service;
    }

    /**
     * POST /api/auth/register
     * Registers a new user account (MANAGER or DRIVER).
     *
     * @Valid triggers Jakarta Bean Validation on the RegisterRequest fields.
     * If any field fails (e.g. blank username, invalid email), Spring throws
     * MethodArgumentNotValidException BEFORE this method body runs — handled below.
     *
     * @RequestBody tells Spring to deserialize the incoming JSON body into a RegisterRequest object.
     *
     * @ResponseStatus(HttpStatus.CREATED) means a successful response returns HTTP 201 Created
     * instead of the default 200 OK — this is the correct REST convention for resource creation.
     *
     * This endpoint is public (no authentication required) — configured in SecurityConfig.
     *
     * @param req the registration data from the request body
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@Valid @RequestBody RegisterRequest req) {
        service.register(req);
    }

    /**
     * GET /api/auth/me
     * Returns the currently authenticated user's username and role.
     *
     * This is used by the frontend after login to know who is logged in
     * and what role they have (to show/hide UI elements accordingly).
     *
     * @AuthenticationPrincipal injects the currently authenticated user's UserDetails
     * object directly into this method. Spring Security populates this from the
     * Authorization header on each request — no session or token needed.
     *
     * userDetails.getAuthorities() returns a collection of GrantedAuthority objects.
     * Each authority is stored as "ROLE_MANAGER" or "ROLE_DRIVER".
     * We strip the "ROLE_" prefix before returning it to the client.
     *
     * @param userDetails the authenticated user injected by Spring Security
     * @return a UserMeResponse containing the username and role (without "ROLE_" prefix)
     */
    @GetMapping("/me")
    public UserMeResponse me(@AuthenticationPrincipal UserDetails userDetails) {
        // getAuthorities() returns all roles/permissions for this user
        // findFirst() gets the first (and only) authority
        // map() strips the "ROLE_" prefix (e.g. "ROLE_MANAGER" → "MANAGER")
        // orElse("DRIVER") is a fallback in case authorities is somehow empty
        String role = userDetails.getAuthorities().stream()
            .findFirst()
            .map(a -> a.getAuthority().replace("ROLE_", ""))
            .orElse("DRIVER");
        return new UserMeResponse(userDetails.getUsername(), role);
    }

    /**
     * Exception handler for IllegalArgumentException.
     *
     * When UserService.register() throws IllegalArgumentException (e.g. "Email already in use"),
     * Spring calls this method instead of returning a generic 500 error.
     *
     * @ExceptionHandler(IllegalArgumentException.class) tells Spring: "if any method in this
     * controller throws IllegalArgumentException, handle it here."
     *
     * ProblemDetail is a standardized RFC 7807 error response format supported by Spring 6+.
     * It produces a JSON body like: { "status": 409, "detail": "Email already in use" }
     *
     * Returns HTTP 409 Conflict — appropriate when a resource already exists.
     *
     * @param ex the thrown exception containing the error message
     * @return a ProblemDetail response with status 409 and the exception message as detail
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleConflict(IllegalArgumentException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setDetail(ex.getMessage());  // e.g. "Email already in use"
        return problem;
    }

    /**
     * Exception handler for @Valid validation failures.
     *
     * When @Valid on a @RequestBody fails (e.g. blank username, invalid email format),
     * Spring throws MethodArgumentNotValidException. This handler catches it and returns
     * a 400 Bad Request with a human-readable list of all field errors.
     *
     * ex.getBindingResult().getFieldErrors() returns a list of all fields that failed validation.
     * We format each as "fieldName: error message" and join them with ", ".
     *
     * Example response detail: "username: must not be blank, email: must be a well-formed email address"
     *
     * @param ex the validation exception containing all field errors
     * @return a ProblemDetail response with status 400 and all validation errors as detail
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        // Build a comma-separated string of all field validation errors
        String detail = ex.getBindingResult().getFieldErrors().stream()
            .map(f -> f.getField() + ": " + f.getDefaultMessage())  // e.g. "email: must be a well-formed email address"
            .collect(Collectors.joining(", "));                      // join multiple errors with ", "
        problem.setDetail(detail);
        return problem;
    }

    /**
     * UserMeResponse — a simple response record returned by the GET /api/auth/me endpoint.
     *
     * This is a nested record (defined inside the controller) because it's only used here.
     * It tells the frontend who is currently logged in and what role they have.
     *
     * Java records auto-generate:
     *   - A constructor: new UserMeResponse(username, role)
     *   - Getters: username() and role()
     *   - equals(), hashCode(), toString()
     *
     * Example JSON response: { "username": "john_doe", "role": "MANAGER" }
     *
     * @param username the authenticated user's login name
     * @param role     the user's role without the "ROLE_" prefix (e.g. "MANAGER" or "DRIVER")
     */
    public record UserMeResponse(String username, String role) {}
}
