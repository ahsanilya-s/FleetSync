package com.fleetsync.fleetsync.ai;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.*;

/**
 * AiController — REST controller for the AI-powered fleet insights endpoint.
 *
 * Exposes endpoints under /api/ai.
 * Restricted to the MANAGER role (enforced in SecurityConfig).
 *
 * This controller acts as a thin layer between the HTTP request and AiService.
 * All the heavy lifting (building context, calling OpenAI) happens in AiService.
 */
@RestController
@RequestMapping("/api/ai")
public class AiController {

    /**
     * AiService handles the actual AI interaction — building the fleet context
     * and sending the question to OpenAI via Spring AI's ChatClient.
     */
    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    /**
     * POST /api/ai/chat
     * Accepts a natural-language question from the fleet manager and returns an
     * AI-generated answer grounded in live fleet data (vehicles, drivers, trips, maintenance).
     *
     * How it works:
     *   1. The manager sends a question in the request body (e.g. "Which vehicle needs service soon?")
     *   2. AiService fetches current fleet data from the database
     *   3. The fleet data is injected into the system prompt sent to OpenAI
     *   4. OpenAI generates a data-driven answer
     *   5. The answer is returned to the manager as JSON
     *
     * Example questions:
     *   "Which vehicle has the highest maintenance cost this month?"
     *   "Summarise overall fleet performance."
     *   "Suggest the best vehicle for a 300 km trip."
     *
     * Returns 200 OK with the AI reply on success.
     * Returns 503 Service Unavailable if the OpenAI API is unreachable (handled below).
     *
     * @param req the request body containing the manager's question
     * @return an AiResponse containing the AI-generated reply text
     */
    @PostMapping("/chat")
    public AiResponse chat(@Valid @RequestBody AiRequest req) {
        String reply = aiService.chat(req.message());  // delegate to AiService
        return new AiResponse(reply);                  // wrap the reply in a JSON-serializable record
    }

    /**
     * Exception handler for any runtime exception thrown by AiService.
     *
     * If OpenAI is down, rate-limited, or the API key is invalid, Spring AI throws
     * a runtime exception. We catch it here and return 503 Service Unavailable
     * instead of a generic 500 Internal Server Error.
     *
     * We intentionally do NOT expose the internal error message (ex.getMessage())
     * to avoid leaking implementation details (e.g. API key errors) to the client.
     *
     * @param ex the exception thrown by AiService
     * @return a ProblemDetail with status 503 and a generic user-friendly message
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleAiError(Exception ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.SERVICE_UNAVAILABLE);
        problem.setDetail("AI service is temporarily unavailable. Please try again later.");
        return problem;
    }
}
