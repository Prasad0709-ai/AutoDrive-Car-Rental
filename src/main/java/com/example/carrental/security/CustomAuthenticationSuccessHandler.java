package com.example.carrental.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if ("ROLE_ADMIN".equals(authority.getAuthority())) {
                response.sendRedirect(request.getContextPath() + "/admin/dashboard");
                return;
            }
        }
        response.sendRedirect(request.getContextPath() + "/dashboard");
    }
}
