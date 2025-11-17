package com.example.premier_league.controller;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http

                .authorizeHttpRequests(auth -> auth
                        // Cho phép truy cập công khai các file tĩnh và các trang không cần login
                        .requestMatchers("/", "/login", "/tournaments/**", "/news/**", "/css/**", "/js/**", "/img/**", "/layout/**").permitAll()
                        // Các trang khác yêu cầu đăng nhập (ví dụ đặt vé)
                        .anyRequest().permitAll() // Tạm thời để permitAll để bạn dễ test, sau này đổi thành authenticated()
                )
                // 2. Cấu hình Đăng nhập Google
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login") // Trang login của bạn
                        .defaultSuccessUrl("/", true) // Đăng nhập thành công về trang chủ
                )
                // 3. Cấu hình Đăng xuất
                .logout(logout -> logout
                        .logoutUrl("/logout") // URL nhận request đăng xuất (khớp với form th:action="@{/logout}")
                        .logoutSuccessUrl("/") // Đăng xuất xong quay về trang chủ
                        .invalidateHttpSession(true) // Hủy session
                        .deleteCookies("JSESSIONID") // Xóa cookie đăng nhập
                        .permitAll()
                );

        return http.build();
    }
}