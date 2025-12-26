package org.th.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.http.HttpMethod;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RateLimitingFilter rateLimitingFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - No authentication required
                        .requestMatchers(
                                "/api/mobile/auth/**",
                                "/api/mobile/feed/**", // ✅ Public feed (Guest personalized)
                                "/api/mobile/shops/**", // ✅ Public shop browsing
                                "/api/mobile/menu-categories/**",
                                "/api/mobile/menu-sub-categories/**",
                                "/api/mobile/locations/**",
                                // "/api/system/**", // System diagnostics - Moved to Admin
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/actuator/**", // Health checks
                                "/v3/api-docs",
                                "/api-docs/**",
                                "/api-docs/swagger-config",
                                "/swagger-resources/**",
                                "/webjars/**")
                        .permitAll()

                        // Reviews: Public to read, Auth to write
                        .requestMatchers(HttpMethod.GET, "/api/mobile/reviews/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/mobile/reviews/**").authenticated()

                        // User features - Authentication required
                        // .requestMatchers("/api/mobile/reviews/**").authenticated() // REPLACED ABOVE
                        // Usually reading is public,
                        // creating is auth. The matches
                        // above are "permits all" but if I
                        // want specific methods to be auth I
                        // need careful ordering or
                        // MethodSecurity. Let's keep it
                        // simple for now, maybe POST is
                        // secured by Controller annotations
                        // or here.
                        // Actually, if I declare permitAll above for /api/mobile/reviews/**, it
                        // overrides this.
                        // I should probably remove the specific permit for reviews if I want mixed
                        // access, or rely on method security.
                        // The existing config had /api/reviews/** in permitAll AND authenticated.
                        // Spring Security first match wins.
                        // So /api/mobile/reviews/** at line 49 will match first and permit all.
                        // If I want to secure write operations, I should rely on @PreAuthorize or
                        // refine this.
                        // For now I will replicate the existing logic but with new paths.

                        .requestMatchers("/api/mobile/favorites/**").authenticated()
                        .requestMatchers("/api/mobile/user/**").authenticated()
                        .requestMatchers("/api/mobile/user/favorites/**").authenticated()
                        .requestMatchers("/api/mobile/cart/**").authenticated()

                        // Maps endpoints - Public but tracked
                        .requestMatchers("/api/maps/**").permitAll()

                        // Admin endpoints
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // .requestMatchers("/api/admin/**").permitAll()

                        // All other endpoints require authentication
                        .anyRequest().authenticated())
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @org.springframework.beans.factory.annotation.Value("${app.security.cors.allowed-origins:*}")
    private String allowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Handle "allow all" explicitly or split by comma
        List<String> origins = "*".equals(allowedOrigins)
                ? List.of("*")
                : Arrays.asList(allowedOrigins.split(","));

        configuration.setAllowedOriginPatterns(origins);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}