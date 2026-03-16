# FleetSync 🚛

A fleet management REST API built with **Spring Boot**, designed to help companies manage vehicles, drivers, trips, and maintenance — with AI-powered insights via OpenAI.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.0.3 |
| Web | Spring MVC (REST API) |
| Database | JDBC (Relational DB) |
| AI | Spring AI + OpenAI |
| Build Tool | Maven |

---

## Getting Started

### Prerequisites
- Java 21+
- Maven 3.8+
- A relational database (e.g., PostgreSQL or MySQL)
- OpenAI API key

### Run the App

```bash
# Clone the repository
git clone https://github.com/ahsanilya-s/FleetSync.git
cd FleetSync

# Run with Maven
./mvnw spring:boot run
```

App will start at `http://localhost:8080`

---

## Project Structure

```
src/
└── main/
    ├── java/com/fleetsync/fleetsync/
    │   └── FleetSyncApplication.java   # Entry point
    └── resources/
        └── application.properties      # App configuration
```

---

## Planned Features

- ✅ Project setup
- ✅ Requirements & user stories
- ⬜ Vehicle CRUD
- ⬜ Driver CRUD
- ⬜ Trip management
- ⬜ Maintenance tracking
- ⬜ AI chat assistant

---

## Documentation

See [REQUIREMENTS.md](./REQUIREMENTS.md) for full user stories, key requirements, and planned API endpoints.

---

## Author

**Ahsan Ilyas** — 8th Semester Project
