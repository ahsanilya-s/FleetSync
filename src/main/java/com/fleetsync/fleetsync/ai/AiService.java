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
import java.util.stream.Collectors;

// Provides AI-powered fleet insights by combining OpenAI with live fleet data
// Answers natural-language questions, summarises performance, and suggests assignments
@Service
public class AiService {

    private static final String SYSTEM_PROMPT = """
        You are FleetSync AI, an intelligent assistant for fleet managers.
        You have access to the current fleet data below and must use it to answer
        questions accurately, suggest optimisations, and provide clear summaries.
        Be concise, data-driven, and professional.

        Fleet Data:
        %s
        """;

    private final ChatClient chatClient;
    private final VehicleRepository vehicleRepo;
    private final DriverRepository driverRepo;
    private final TripRepository tripRepo;
    private final MaintenanceRepository maintenanceRepo;

    public AiService(
            ChatClient.Builder chatClientBuilder,
            VehicleRepository vehicleRepo,
            DriverRepository driverRepo,
            TripRepository tripRepo,
            MaintenanceRepository maintenanceRepo) {
        this.chatClient = chatClientBuilder.build();
        this.vehicleRepo = vehicleRepo;
        this.driverRepo = driverRepo;
        this.tripRepo = tripRepo;
        this.maintenanceRepo = maintenanceRepo;
    }

    // Sends the manager's question to OpenAI together with a structured snapshot of
    // the current fleet (vehicles, drivers, trips, maintenance) as system context.
    // Returns the AI-generated response text.
    public String chat(String question) {
        String fleetContext = buildFleetContext();
        String systemMessage = SYSTEM_PROMPT.formatted(fleetContext);

        return chatClient.prompt()
                .system(systemMessage)
                .user(question)
                .call()
                .content();
    }

    // Builds a human-readable summary of current fleet data to inject into the prompt.
    // Keeps the context concise so it fits within OpenAI's context window.
    private String buildFleetContext() {
        StringBuilder sb = new StringBuilder();

        // --- Vehicles ---
        List<Vehicle> vehicles = vehicleRepo.findAll();
        sb.append("=== Vehicles (").append(vehicles.size()).append(") ===\n");
        if (vehicles.isEmpty()) {
            sb.append("No vehicles registered.\n");
        } else {
            vehicles.forEach(v -> sb.append(
                "ID=").append(v.id())
                .append(", Name=").append(v.name())
                .append(", Type=").append(v.type())
                .append(", Plate=").append(v.plateNumber())
                .append(", FuelType=").append(v.fuelType())
                .append(", Status=").append(v.status())
                .append("\n"));
        }

        // --- Drivers ---
        List<Driver> drivers = driverRepo.findAll();
        sb.append("\n=== Drivers (").append(drivers.size()).append(") ===\n");
        if (drivers.isEmpty()) {
            sb.append("No drivers registered.\n");
        } else {
            drivers.forEach(d -> sb.append(
                "ID=").append(d.id())
                .append(", Name=").append(d.fullName())
                .append(", Status=").append(d.status())
                .append("\n"));
        }

        // --- Trips (most recent 200) ---
        List<Trip> trips = tripRepo.findAll();
        // Cap context to avoid exceeding OpenAI token limits on large fleets
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

        // --- Maintenance ---
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
                .append(m.description() != null ? ", Desc=" + m.description() : "")
                .append("\n"));
        }

        return sb.toString();
    }
}
