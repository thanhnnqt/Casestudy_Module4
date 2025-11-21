package com.example.premier_league.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.util.Set;

@Configuration
public class CustomAdminSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // Lấy danh sách các quyền (Roles) của user vừa đăng nhập
        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());

        if (roles.contains("ROLE_ADMIN")) {
            // Nếu là Admin tổng -> Vào trang quản trị hệ thống
            response.sendRedirect("/admin/home");
        } else if (roles.contains("ROLE_OWNER")) {
            // Nếu là Chủ đội bóng -> Vào trang Dashboard của chủ đội bóng
            response.sendRedirect("/admin/owner/dashboard");
        } else {
            // Các trường hợp khác
            response.sendRedirect("/");
        }
    }
}