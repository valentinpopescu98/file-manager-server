package com.valentin.file_manager_server.security;

import com.valentin.file_manager_server.model.AppUser;
import com.valentin.file_manager_server.service.AppUserDetailsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    @Value("${api.client.url}")
    private String clientUrl;

    private final JwtUtil jwtUtil;
    private final AppUserDetailsService userService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;
        String email = authToken.getPrincipal().getAttribute("email");

        if (email == null) {
            log.warn("OAuth2 provider did not return an email");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Email not available from provider");
            return;
        }

        AppUser user;
        try {
            user = userService.getOrCreateOAuth2User(email);
        } catch (Exception e) {
            log.error("Error during user registration", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "User registration failed");
            return;
        }

        String jwt = jwtUtil.generateToken(user);
        String redirectUrl = UriComponentsBuilder
                .fromUriString(clientUrl + "/oauth2/success")
                .queryParam("token", jwt)
                .build()
                .toUriString();
        response.sendRedirect(redirectUrl);
    }
}
