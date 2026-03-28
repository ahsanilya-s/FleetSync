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
        // Spring Security 6.4+: UserDetailsService is required in the constructor
        // Spring Security 7 requires UserDetailsService via constructor
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userService);
        // Tells Spring Security how to verify the submitted password against the stored hash
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // Exposes the AuthenticationManager bean — required for programmatic authentication if needed
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // Defines the HTTP security rules for the application
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF — not needed for stateless REST APIs
            .csrf(csrf -> csrf.disable())

            // Register our custom authentication provider
            .authenticationProvider(authenticationProvider())

            .authorizeHttpRequests(auth -> auth
                // Allow anyone to register a new account without authentication
                .requestMatchers("/api/auth/register").permitAll()

                // Allow both MANAGER and DRIVER to update a trip's status
                // (drivers mark trips as started or completed)
                .requestMatchers(HttpMethod.PUT, "/api/trips/**/status").hasAnyRole("MANAGER", "DRIVER")

                // Restrict all other vehicle, driver, and trip endpoints to MANAGER role only
                // Restrict all vehicle, driver, trip, maintenance, and AI endpoints to MANAGER role only
                // Any other role (e.g. DRIVER) or unauthenticated request will get 403
                .requestMatchers("/api/vehicles/**", "/api/drivers/**", "/api/trips/**",
                                 "/api/maintenance/**", "/api/ai/**").hasRole("MANAGER")
                // Restrict all vehicle, driver, trip, and maintenance endpoints to MANAGER role only
                // Any other role (e.g. DRIVER) or unauthenticated request will get 403
                .requestMatchers("/api/vehicles/**", "/api/drivers/**", "/api/trips/**", "/api/maintenance/**").hasRole("MANAGER")

                // All other requests also require authentication
                .anyRequest().authenticated()
            )

            // Enable HTTP Basic authentication — credentials sent as Base64 in Authorization header
            // Format: Authorization: Basic <base64(username:password)>
            .httpBasic(basic -> {});

        return http.build();
    }
}
