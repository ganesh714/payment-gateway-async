package com.gateway.config;

import com.gateway.models.Merchant;
import com.gateway.repositories.MerchantRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private MerchantRepository merchantRepository;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/health", "/api/v1/test/**",
                                "/api/v1/orders/*/public",
                                "/api/v1/payments/public",
                                "/api/v1/payments/*/public")
                        .permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(new ApiKeyAuthFilter(merchantRepository), BasicAuthenticationFilter.class);

        return http.build();
    }

    public static class ApiKeyAuthFilter extends OncePerRequestFilter {

        private final MerchantRepository merchantRepository;

        public ApiKeyAuthFilter(MerchantRepository merchantRepository) {
            this.merchantRepository = merchantRepository;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                FilterChain filterChain)
                throws ServletException, IOException {

            String path = request.getRequestURI();
            if (path.startsWith("/health") || path.startsWith("/api/v1/test") || path.contains("/public")) {
                filterChain.doFilter(request, response);
                return;
            }

            String apiKey = request.getHeader("X-Api-Key");
            String apiSecret = request.getHeader("X-Api-Secret");

            System.out.println("DEBUG: Auth Filter - Request Path: " + path);
            System.out.println("DEBUG: Auth Filter - Headers: Key=" + (apiKey != null ? "***" : "null") + ", Secret="
                    + (apiSecret != null ? "***" : "null"));

            if (apiKey == null || apiSecret == null) {
                System.out.println("DEBUG: Auth Filter - Missing Credentials");
                sendError(response, "AUTHENTICATION_ERROR", "Missing API credentials");
                return;
            }

            Optional<Merchant> merchantOpt = merchantRepository.findByApiKey(apiKey);
            if (merchantOpt.isEmpty()) {
                System.out.println("DEBUG: Auth Filter - Merchant not found for key: " + apiKey);
            } else if (!merchantOpt.get().getApiSecret().equals(apiSecret)) {
                System.out.println("DEBUG: Auth Filter - Secret mismatch. Expected: " + merchantOpt.get().getApiSecret()
                        + ", Received: " + apiSecret);
            }

            if (merchantOpt.isEmpty() || !merchantOpt.get().getApiSecret().equals(apiSecret)) {
                sendError(response, "AUTHENTICATION_ERROR", "Invalid API credentials");
                return;
            }

            org.springframework.security.core.Authentication auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                    merchantOpt.get(), null, java.util.Collections.emptyList());
            org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);

            filterChain.doFilter(request, response);
        }

        private void sendError(HttpServletResponse response, String code, String description) throws IOException {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            Map<String, Object> error = new HashMap<>();
            error.put("code", code);
            error.put("description", description);
            Map<String, Object> body = new HashMap<>();
            body.put("error", error);

            PrintWriter out = response.getWriter();
            new ObjectMapper().writeValue(out, body);
            out.flush();
        }
    }
}
