package com.fleetsync.fleetsync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * FleetSyncApplication — the entry point of the entire Spring Boot application.
 *
 * When you run this class, Spring Boot:
 *   1. Scans all classes under this package for annotations like @RestController,
 *      @Service, @Repository, @Configuration, etc.
 *   2. Auto-configures the embedded web server (Tomcat), database connection,
 *      security, and any other dependencies found on the classpath.
 *   3. Starts the HTTP server on port 8080 (configurable in application.properties).
 *
 * @SpringBootApplication is a shortcut that combines three annotations:
 *   - @Configuration      → marks this class as a source of Spring bean definitions
 *   - @EnableAutoConfiguration → tells Spring Boot to auto-configure beans based on
 *                                 what's on the classpath (e.g. DataSource, Security)
 *   - @ComponentScan      → scans this package and all sub-packages for Spring components
 */
@SpringBootApplication
public class FleetSyncApplication {

    /**
     * main() is the standard Java entry point.
     * SpringApplication.run() bootstraps the entire application context and starts the server.
     *
     * @param args command-line arguments (e.g. --server.port=9090) — passed through to Spring
     */
    public static void main(String[] args) {
        SpringApplication.run(FleetSyncApplication.class, args);
    }
}
