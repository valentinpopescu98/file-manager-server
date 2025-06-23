package com.valentin.file_manager_server.security;

import com.valentin.file_manager_server.security.jwt.JwtAuthFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${api.client.url}")
    private String clientUrl;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http, JwtAuthFilter jwtFilter,
            UserDetailsService userDetailsService, AuthenticationSuccessHandler oAuth2SuccessHandler)
            throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .httpBasic(auth -> auth.disable())
                .formLogin(form -> form.disable())
                .userDetailsService(userDetailsService)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(HttpMethod.GET, "/api").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/download").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/upload").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/delete").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/verify-email").hasRole("ADMIN")
                        .requestMatchers("/api/login", "/api/register",
                                "/login/oauth2/code/**", "/oauth2/success",
                                "/css/**", "/js/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oAuth2SuccessHandler)
                        .authorizationEndpoint(auth -> auth
                                .baseUri("/oauth2/authorization")
                        )
                        .loginPage("/login")
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(restAuthenticationEntryPoint())
                        .accessDeniedHandler(restAuthenticationDeniedHandler())
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private AuthenticationEntryPoint restAuthenticationEntryPoint() {
        return (request, response, authException) -> {
            if (!response.isCommitted()) {
                log.warn("Auth entry point triggered: {}", authException.getMessage());

                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");

                String jsonResponse = "{\"error\": \"Unauthorized\", \"message\": \""
                        + authException.getMessage() + "\"}";

                response.getWriter().write(jsonResponse);
                response.getWriter().flush();
            } else {
                log.warn("Auth entry point: response already committed");
            }
        };
    }

    private AccessDeniedHandler restAuthenticationDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            if (!response.isCommitted()) {
                log.warn("Access denied: {}", accessDeniedException.getMessage());

                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");

                String jsonResponse = "{\"error\": \"Access Denied\", \"message\": \""
                        + accessDeniedException.getMessage() + "\"}";

                response.getWriter().write(jsonResponse);
                response.getWriter().flush();
            } else {
                log.warn("Access denied: response already committed");
            }
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(clientUrl));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("*"));
        config.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
