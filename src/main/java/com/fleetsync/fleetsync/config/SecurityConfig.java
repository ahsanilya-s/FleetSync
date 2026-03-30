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

@Configuration
public class SecurityConfig {

    // UserService is injected lazily to avoid circular dependency
    // (UserService depends on BCryptPasswordEncoder which is defined here)
    private final UserService userService;

    public SecurityConfig(@Lazy UserService userService) {
        this.userService = userService;
    }

    // Registers BCryptPasswordEncoder as a bean with strength 12
    // Used by UserService to hash passwords before storing them
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    // Wires Spring Security's DaoAuthenticationProvider with our UserDetailsService
    // and password encoder so it can validate credentials on each request
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // Exposes the AuthenticationManager bean — required for programmatic authentication if needed
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // Allows the React frontend (Vite dev server on port 5173) to call the API
    // Must be registered here — spring.mvc.cors properties are ignored when Spring Security is active
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        // Allow the browser to send the Authorization header (Basic Auth)
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // Defines the HTTP security rules for the application
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF — not needed for stateless REST APIs
            .csrf(csrf -> csrf.disable())

            // Apply CORS config defined above
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // Register our custom authentication provider
            .authenticationProvider(authenticationProvider())

            .authorizeHttpRequests(auth -> auth
                // Allow anyone to register a new account without authentication
                .requestMatchers("/api/auth/register").permitAll()

                // Allow authenticated users to fetch their own profile
                .requestMatchers("/api/auth/me").authenticated()

                // Allow both MANAGER and DRIVER to update a trip's status
                .requestMatchers(HttpMethod.PUT, "/api/trips/*/status").hasAnyRole("MANAGER", "DRIVER")

                // Allow DRIVER to read their own trips
                .requestMatchers(HttpMethod.GET, "/api/trips").hasAnyRole("MANAGER", "DRIVER")

                // Restrict all other vehicle, driver, trip, maintenance, and AI endpoints to MANAGER only
                .requestMatchers("/api/vehicles/**", "/api/drivers/**", "/api/trips/**",
                                 "/api/maintenance/**", "/api/ai/**").hasRole("MANAGER")

                .anyRequest().authenticated()
            )

            // Enable HTTP Basic authentication — credentials sent as Base64 in Authorization header
            .httpBasic(basic -> {});

        return http.build();
    }
}
