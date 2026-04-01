package com.fleetsync.fleetsync.config;

import org.springframework.http.HttpMethod;
import com.fleetsync.fleetsync.user.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * SecurityConfig — central security configuration for the FleetSync API.
 *
 * This class tells Spring Security:
 *   - How to hash and verify passwords (BCrypt)
 *   - How to load users from the database during login
 *   - Which API endpoints are public vs. protected
 *   - Which roles are allowed to access which endpoints
 *   - How to handle CORS so the React frontend can call the API
 *
 * @Configuration tells Spring to treat this class as a source of bean definitions.
 * Every method annotated with @Bean will be registered in the Spring application context
 * and can be injected into other classes.
 */
@Configuration
public class SecurityConfig {

    /**
     * UserService is injected with @Lazy to break a circular dependency:
     *   SecurityConfig creates BCryptPasswordEncoder → UserService needs BCryptPasswordEncoder
     *   → UserService is also needed by SecurityConfig (as UserDetailsService).
     *
     * @Lazy tells Spring: "don't create UserService until the first time it's actually needed",
     * which breaks the circular creation chain.
     */
    private final UserService userService;

    public SecurityConfig(@Lazy UserService userService) {
        this.userService = userService;
    }

    /**
     * Registers BCryptPasswordEncoder as a Spring bean.
     *
     * BCrypt is a one-way hashing algorithm designed for passwords.
     * The strength parameter (12) controls how many rounds of hashing are applied —
     * higher = slower to crack but also slower to compute. 12 is a good production default.
     *
     * This bean is injected into UserService so it can hash passwords before saving them.
     *
     * @return a BCryptPasswordEncoder with strength 12
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Wires Spring Security's DaoAuthenticationProvider with our custom UserDetailsService
     * (UserService) and the BCrypt password encoder.
     *
     * When a login request arrives, Spring Security calls this provider which:
     *   1. Calls userService.loadUserByUsername() to fetch the user from the DB
     *   2. Uses BCrypt to compare the incoming plain-text password with the stored hash
     *   3. Grants or denies access based on the result
     *
     * @return a configured DaoAuthenticationProvider
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        // Pass UserService as the UserDetailsService implementation
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userService);
        // Tell the provider to use BCrypt when verifying passwords
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Exposes the AuthenticationManager as a Spring bean.
     *
     * AuthenticationManager is the main entry point for programmatic authentication.
     * Spring Security creates it internally; we just expose it here so it can be
     * injected elsewhere if needed (e.g. a custom login endpoint).
     *
     * @param config Spring's AuthenticationConfiguration — auto-provided by Spring
     * @return the application's AuthenticationManager
     * @throws Exception if the manager cannot be built
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Configures CORS (Cross-Origin Resource Sharing) so the React frontend
     * running on http://localhost:5173 (Vite dev server) can call this API.
     *
     * Without this, browsers block cross-origin requests by default.
     * Note: spring.mvc.cors properties in application.properties are IGNORED when
     * Spring Security is active — CORS must be configured here instead.
     *
     * @return a CorsConfigurationSource that applies the CORS rules to all routes ("/**")
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Only allow requests from the React dev server
        config.setAllowedOrigins(List.of("http://localhost:5173"));

        // Allow standard REST methods plus OPTIONS (used for preflight checks)
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Allow the Authorization header (needed for HTTP Basic) and Content-Type
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        // Allow the browser to send credentials (Authorization header) with requests
        config.setAllowCredentials(true);

        // Apply this CORS config to every URL in the application
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * Defines the HTTP security filter chain — the core of Spring Security configuration.
     *
     * This method sets up:
     *   - CSRF protection (disabled — not needed for stateless REST APIs)
     *   - CORS rules (applied from corsConfigurationSource above)
     *   - Authentication provider (our DaoAuthenticationProvider)
     *   - Authorization rules (which roles can access which endpoints)
     *   - Authentication mechanism (HTTP Basic — credentials in Authorization header)
     *
     * @param http the HttpSecurity builder provided by Spring Security
     * @return the built SecurityFilterChain
     * @throws Exception if the chain cannot be built
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF protection — CSRF attacks target browser-based session cookies.
            // Since we use stateless HTTP Basic auth (no cookies), CSRF is not a threat here.
            .csrf(csrf -> csrf.disable())

            // Apply the CORS configuration defined in corsConfigurationSource()
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // Register our custom DaoAuthenticationProvider
            .authenticationProvider(authenticationProvider())

            .authorizeHttpRequests(auth -> auth
                // Anyone (even unauthenticated) can call POST /api/auth/register to create an account
                .requestMatchers("/api/auth/register").permitAll()

                // Any authenticated user (MANAGER or DRIVER) can call GET /api/auth/me
                // to retrieve their own username and role
                .requestMatchers("/api/auth/me").authenticated()

                // Both MANAGER and DRIVER can update a trip's status (e.g. driver marks trip as IN_PROGRESS)
                .requestMatchers(HttpMethod.PUT, "/api/trips/*/status").hasAnyRole("MANAGER", "DRIVER")

                // Both MANAGER and DRIVER can read trips (driver views their own assignments)
                .requestMatchers(HttpMethod.GET, "/api/trips").hasAnyRole("MANAGER", "DRIVER")

                // All other vehicle, driver, trip, maintenance, and AI endpoints are MANAGER-only
                .requestMatchers("/api/vehicles/**", "/api/drivers/**", "/api/trips/**",
                                 "/api/maintenance/**", "/api/ai/**").hasRole("MANAGER")

                // Any other request not matched above requires authentication
                .anyRequest().authenticated()
            )

            // Enable HTTP Basic authentication.
            // The client sends credentials as: Authorization: Basic Base64(username:password)
            // Spring Security decodes this and passes it to our DaoAuthenticationProvider.
            .httpBasic(basic -> {});

        return http.build();
    }
}
