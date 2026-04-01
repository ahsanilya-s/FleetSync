package com.fleetsync.fleetsync.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * AiRequest — represents the JSON body sent by the fleet manager to the AI chat endpoint.
 *
 * This is a DTO used to carry the manager's question from the HTTP request into AiController.
 * Spring deserializes the incoming JSON into this record automatically.
 *
 * Example JSON body:
 * { "message": "Which vehicle has the highest maintenance cost this month?" }
 */
public record AiRequest(

    /**
     * The natural-language question or instruction the manager wants the AI to answer.
     * @NotBlank — must not be null or empty (can't send a blank question)
     * @Size     — max 2000 characters to prevent excessively large prompts
     */
    @NotBlank
    @Size(max = 2000)
    String message
) {}
