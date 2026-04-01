package com.fleetsync.fleetsync.ai;

import com.fleetsync.fleetsync.driver.Driver;
import com.fleetsync.fleetsync.driver.DriverRepository;
import com.fleetsync.fleetsync.maintenance.Maintenance;
import com.fleetsync.fleetsync.maintenance.MaintenanceRepository;
import com.fleetsync.fleetsync.trip.Trip;
import com.fleetsync.fleetsync.trip.TripRepository;
import com.fleetsync.fleetsync.vehicle.Vehicle;
import com.fleetsync.fleetsync.vehicle.VehicleRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AiService — provides AI-powered fleet insights by combining OpenAI with live fleet data.
 *
 * How it works:
 *   1. The manager sends a question (e.g. "Which vehicle needs service soon?")
 *   2. buildFleetContext() fetches current data from all four repositories
 *   3. The fleet data is formatted as plain text and injected into the system prompt
 *   4. The question + system prompt are sent to OpenAI via Spring AI's ChatClient
 *   5. OpenAI returns a data-driven answer which is passed back to the controller
 *
 * Spring AI abstracts the OpenAI API — we just call chatClient.prompt()...call().content()
 * and Spring AI handles authentication, serialization, and HTTP communication.
 *
 * @Service marks this as a Spring-managed service component.
 */
@Service
public class AiService {

    /**
     * The system prompt template sent to OpenAI with every request.
     * It defines the AI's persona and injects the live fleet data snapshot.
     *
     * %s is a placeholder replaced by the fleet context string at runtime
     * using String.formatted() in the chat() method.
     *
     * The system prompt is sent as the "system" role message — it sets the AI's
     * behavior and context before the user's question is processed.
     */
    private static final String SYSTEM_PROMPT = """
        You are FleetSync AI, an intelligent assistant for fleet managers.
        You have access to the current fleet data below and must use it to answer
        questions accurately, suggest optimisations, and provide clear summaries.
        Be concise, data-driven, and professional.

        Fleet Data:
        %s
        """;

    /**
     * ChatClient is Spring AI's abstraction for calling AI models (OpenAI, etc.).
     * It's built from ChatClient.Builder which is auto-configured by Spring AI
     * using the API key and model settings from application.properties.
     */
    private final ChatClient chatClient;

    // Repositories used to fetch live fleet data for the AI context
    private final VehicleRepository vehicleRepo;
    private final DriverRepository driverRepo;
    private final TripRepository tripRepo;
    private final MaintenanceRepository maintenanceRepo;

    /**
     * Constructor injection — Spring provides all dependencies automatically.
     * ChatClient.Builder is auto-configured by Spring AI based on application.properties settings
     * (spring.ai.openai.api-key, spring.ai.openai.chat.options.model, etc.).
     */
    public AiService(
            ChatClient.Builder chatClientBuilder,
            VehicleRepository vehicleRepo,
            DriverRepository driverRepo,
            TripRepository tripRepo,
            MaintenanceRepository maintenanceRepo) {
        this.chatClient = chatClientBuilder.build();  // build the ChatClient from the auto-configured builder
        this.vehicleRepo = vehicleRepo;
        this.driverRepo = driverRepo;
        this.tripRepo = tripRepo;
        this.maintenanceRepo = maintenanceRepo;
    }

    /**
     * Sends the manager's question to OpenAI together with a live fleet data snapshot.
     * Returns the AI-generated response text.
     *
     * The fleet context is rebuilt on every call to ensure the AI always has
     * the most up-to-date data (no caching).
     *
     * @param question the natural-language question from the fleet manager
     * @return the AI-generated answer as a plain string
     */
    public String chat(String question) {
        // Build a text snapshot of the current fleet state
        String fleetContext = buildFleetContext();

        // Inject the fleet data into the system prompt template
        String systemMessage = SYSTEM_PROMPT.formatted(fleetContext);

        // Send the system message + user question to OpenAI and return the response text
        return chatClient.prompt()
                .system(systemMessage)   // sets the AI's context and persona
                .user(question)          // the manager's actual question
                .call()                  // sends the request to OpenAI
                .content();              // extracts the response text from the API response
    }

    /**
     * Builds a human-readable text summary of the current fleet state.
     * This text is injected into the OpenAI system prompt so the AI can answer
     * questions based on real data rather than making things up.
     *
     * Sections included:
     *   - Vehicles (all)
     *   - Drivers (all)
     *   - Trips (most recent 200 — capped to avoid token limit)
     *   - Maintenance records (most recent 200 — capped in MaintenanceRepository.findAll())
     *
     * StringBuilder is used for efficient string concatenation in a loop.
     * (Using + in a loop creates many temporary String objects — StringBuilder avoids this.)
     *
     * @return a formatted string containing all fleet data sections
     */
    private String buildFleetContext() {
        StringBuilder sb = new StringBuilder();

        // --- Vehicles section ---
        List<Vehicle> vehicles = vehicleRepo.findAll();
        sb.append("=== Vehicles (").append(vehicles.size()).append(") ===\n");
        if (vehicles.isEmpty()) {
            sb.append("No vehicles registered.\n");
        } else {
            // Append one line per vehicle with key fields
            vehicles.forEach(v -> sb.append(
                "ID=").append(v.id())
                .append(", Name=").append(v.name())
                .append(", Type=").append(v.type())
                .append(", Plate=").append(v.plateNumber())
                .append(", FuelType=").append(v.fuelType())
                .append(", Status=").append(v.status())  // AVAILABLE or ON_TRIP
                .append("\n"));
        }

        // --- Drivers section ---
        List<Driver> drivers = driverRepo.findAll();
        sb.append("\n=== Drivers (").append(drivers.size()).append(") ===\n");
        if (drivers.isEmpty()) {
            sb.append("No drivers registered.\n");
        } else {
            drivers.forEach(d -> sb.append(
                "ID=").append(d.id())
                .append(", Name=").append(d.fullName())
                .append(", Status=").append(d.status())  // AVAILABLE or ON_TRIP
                .append("\n"));
        }

        // --- Trips section (capped at 200 to stay within OpenAI token limits) ---
        List<Trip> trips = tripRepo.findAll();
        // If there are more than 200 trips, only take the first 200 (most recent)
        List<Trip> recentTrips = trips.size() > 200 ? trips.subList(0, 200) : trips;
        sb.append("\n=== Trips (showing ").append(recentTrips.size())
          .append(" of ").append(trips.size()).append(") ===\n");
        if (recentTrips.isEmpty()) {
            sb.append("No trips recorded.\n");
        } else {
            recentTrips.forEach(t -> sb.append(
                "ID=").append(t.id())
                .append(", VehicleID=").append(t.vehicleId())
                .append(", DriverID=").append(t.driverId())
                .append(", From=").append(t.origin())
                .append(", To=").append(t.destination())
                .append(", Status=").append(t.status())
                .append(", CreatedAt=").append(t.createdAt())
                .append("\n"));
        }

        // --- Maintenance section (already capped at 200 in MaintenanceRepository.findAll()) ---
        List<Maintenance> records = maintenanceRepo.findAll();
        sb.append("\n=== Maintenance Records (").append(records.size()).append(") ===\n");
        if (records.isEmpty()) {
            sb.append("No maintenance records found.\n");
        } else {
            records.forEach(m -> sb.append(
                "ID=").append(m.id())
                .append(", VehicleID=").append(m.vehicleId())
                .append(", Date=").append(m.date())
                .append(", Type=").append(m.type())
                .append(", Cost=").append(m.cost())
                // Only append description if it's not null (it's optional)
                .append(m.description() != null ? ", Desc=" + m.description() : "")
                .append("\n"));
        }

        return sb.toString();
    }
}
