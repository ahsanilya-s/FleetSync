package com.fleetsync.fleetsync.ai;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.*;

// REST controller for the AI-powered fleet insights endpoint
// Restricted to MANAGER role (enforced in SecurityConfig)
@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    // POST /api/ai/chat
    // Accepts a natural-language question from the fleet manager and returns an
    // AI-generated answer grounded in live fleet data (vehicles, trips, maintenance).
    //
    // Example questions:
    //   "Which vehicle has the highest maintenance cost this month?"
    //   "Summarise overall fleet performance."
    //   "Suggest the best vehicle for a 300 km trip."
    //
    // Returns 200 OK with the AI reply on success.
    // Returns 503 Service Unavailable if the OpenAI API is unreachable.
    @PostMapping("/chat")
    public AiResponse chat(@Valid @RequestBody AiRequest req) {
        String reply = aiService.chat(req.message());
        return new AiResponse(reply);
    }

    // Catches any runtime exception from the AI service (e.g. OpenAI API errors)
    // and returns 503 so the client knows the AI is temporarily unavailable.
    // The internal error message is not exposed to avoid leaking implementation details.
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleAiError(Exception ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.SERVICE_UNAVAILABLE);
        problem.setDetail("AI service is temporarily unavailable. Please try again later.");
        return problem;
    }
}
