package com.fleetsync.fleetsync.user;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@Valid @RequestBody RegisterRequest req) {
        service.register(req);
    }

    // Catches duplicate email/username from UserService and returns 409
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleConflict(IllegalArgumentException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setDetail(ex.getMessage());
        return problem;
    }
}
