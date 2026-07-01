package com.farmtofolk.farmtofolk_ledger.common.config;

import com.farmtofolk.farmtofolk_ledger.auth.JwtAuthenticationFilter;
import com.farmtofolk.farmtofolk_ledger.auth.UserRepository;
import com.farmtofolk.farmtofolk_ledger.common.error.ApiErrorResponse;
import com.farmtofolk.farmtofolk_ledger.common.error.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  UserDetailsService userDetailsService(UserRepository userRepository) {
    return username ->
        userRepository
            .findByEmailIgnoreCase(username)
            .or(() -> userRepository.findByPhone(username))
            .map(
                user ->
                    org.springframework.security.core.userdetails.User.withUsername(
                            user.getId().toString())
                        .password(user.getPasswordHash())
                        .roles(user.getRole().name())
                        .disabled(!Boolean.TRUE.equals(user.getActive()))
                        .build())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
  }

  @Bean
  SecurityFilterChain securityFilterChain(
      HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter, ObjectMapper objectMapper)
      throws Exception {
    http.csrf(csrf -> csrf.disable())
        .cors(Customizer.withDefaults())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(
            errors ->
                errors
                    .authenticationEntryPoint(
                        (request, response, exception) -> {
                          response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                          response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                          objectMapper.writeValue(
                              response.getOutputStream(),
                              new ApiErrorResponse(
                                  LocalDateTime.now(),
                                  401,
                                  "Unauthorized",
                                  "Authentication required",
                                  request.getRequestURI()));
                        })
                    .accessDeniedHandler(
                        (request, response, exception) -> {
                          response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                          response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                          objectMapper.writeValue(
                              response.getOutputStream(),
                              new ApiErrorResponse(
                                  LocalDateTime.now(),
                                  403,
                                  "Forbidden",
                                  "Access denied",
                                  request.getRequestURI()));
                        }))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(HttpMethod.OPTIONS, "/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/auth/login")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/public/**")
                    .permitAll()
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**")
                    .permitAll()
                    .requestMatchers("/api/admin/**")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PATCH, "/api/users/me/password")
                    .authenticated()
                    .requestMatchers("/api/farmer-dashboard/**")
                    .hasAnyRole("ADMIN", "FARMER")
                    .requestMatchers(HttpMethod.POST, "/api/batches/*/qr-code")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/batches/*/price-breakdown")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/batches/*/price-breakdown")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/batches/*/procurement")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/batches/*/procurement")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/api/batches/*/procurement")
                    .hasAnyRole("ADMIN", "FIELD_OFFICER", "FARMER")
                    .requestMatchers(HttpMethod.POST, "/api/batches/*/sale-transactions")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/api/batches/*/sale-transactions")
                    .hasAnyRole("ADMIN", "FIELD_OFFICER", "FARMER")
                    .requestMatchers(HttpMethod.DELETE, "/api/**")
                    .hasRole("ADMIN")
                    .requestMatchers(
                        HttpMethod.POST,
                        "/api/farmers/**",
                        "/api/farms/**",
                        "/api/batches/**",
                        "/api/verifications/**")
                    .hasAnyRole("ADMIN", "FIELD_OFFICER")
                    .requestMatchers(
                        HttpMethod.PUT,
                        "/api/farmers/**",
                        "/api/farms/**",
                        "/api/batches/**",
                        "/api/verifications/**")
                    .hasAnyRole("ADMIN", "FIELD_OFFICER")
                    .requestMatchers(HttpMethod.PATCH, "/api/farmers/**")
                    .hasAnyRole("ADMIN", "FIELD_OFFICER")
                    .requestMatchers(HttpMethod.GET, "/api/**")
                    .hasAnyRole("ADMIN", "FIELD_OFFICER")
                    .anyRequest()
                    .authenticated())
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}
