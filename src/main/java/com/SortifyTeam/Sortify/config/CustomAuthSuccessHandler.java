package com.SortifyTeam.Sortify.config;

import jakarta.servlet.http.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class CustomAuthSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        String username = authentication.getName();
        String role = authentication.getAuthorities().stream()
                .findFirst().map(a -> a.getAuthority()).orElse("UNKNOWN");

        log.info("==================================================");
        log.info("[LOGIN SUCCESS] User: {} dengan Role: {})" ,  username, role);
        log.info("==================================================");

        switch (role) {
            case "ROLE_ADMIN" ->    response.sendRedirect("/admin/dashboard");
            case "ROLE_PETUGAS" ->  response.sendRedirect("/petugas/dashboard");
            default ->              response.sendRedirect("/warga/dashboard");
        }
    }
}

