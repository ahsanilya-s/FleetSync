package com.fleetsync.fleetsync.ai;

/**
 * AiResponse — wraps the AI-generated text returned to the fleet manager.
 *
 * This is a simple response DTO. Spring serializes it to JSON automatically.
 *
 * Example JSON response:
 * { "reply": "Vehicle #3 (Truck Alpha) has the highest maintenance cost at $450 this month." }
 *
 * @param reply the AI-generated answer text from OpenAI
 */
public record AiResponse(String reply) {}
