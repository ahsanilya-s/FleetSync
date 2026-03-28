package com.fleetsync.fleetsync.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Represents the JSON body sent by the fleet manager to the AI chat endpoint
public record AiRequest(

    // The question or instruction the manager wants the AI to answer
    @NotBlank
    @Size(max = 2000)
    String message
) {}
