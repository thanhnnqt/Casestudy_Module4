package com.example.premier_league.config;

import com.example.premier_league.entity.Account;
import com.example.premier_league.repository.IAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.thymeleaf.extras.springsecurity6.dialect.SpringSecurityDialect;

import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Autowired
    private CustomAuthFailureHandler customAuthFailureHandler;

    @Autowired
    private IAccountRepository accountRepository;

    // ... (Các Bean springSecurityDialect, passwordEncoder, authenticationManager giữ nguyên) ...
    @Bean
    public SpringSecurityDialect springSecurityDialect() {
        return new SpringSecurityDialect();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            Account account = accountRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
            return new User(
                    account.getUsername(),
                    account.getPassword(),
                    account.getRoles().stream()
                            .map(role -> new SimpleGrantedAuthority(role.getRoleName()))
                            .collect(Collectors.toSet())
            );
        };
    }

    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // ==================================================================
    // CẤU HÌNH 1: DÀNH RIÊNG CHO ADMIN (Order 1 - Chạy trước)
    // ==================================================================
    @Bean
    @Order(1)
    public SecurityFilterChain adminFilterChain(HttpSecurity http, AuthenticationProvider authProvider) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);

        http.securityMatcher("/admin/**");

        http.authenticationProvider(authProvider);

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin/login", "/admin/process-login", "/admin/logout").permitAll()
                // Các trang admin khác bắt buộc phải có ROLE_ADMIN
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
        );

        http.formLogin(form -> form
                // SỬA: Đổi /admin/admin-login thành /admin/login
                .loginPage("/admin/login")
                .loginProcessingUrl("/admin/process-login")
                .defaultSuccessUrl("/admin/home", true)
                .failureUrl("/admin/login?error")
                .usernameParameter("username")
                .passwordParameter("password")
                .permitAll()
        );

        http.logout(logout -> logout
                .logoutUrl("/admin/logout")
                .logoutSuccessUrl("/admin/login?logout") // SỬA: Đảm bảo logout xong về đúng trang login
                .deleteCookies("JSESSIONID")
                .permitAll()
        );

        return http.build();
    }

    // ==================================================================
    // CẤU HÌNH 2: DÀNH CHO USER & CÔNG KHAI (Order 2 - Chạy sau)
    // ==================================================================
    @Bean
    @Order(2)
    public SecurityFilterChain userFilterChain(HttpSecurity http, AuthenticationProvider authProvider) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);
        http.authenticationProvider(authProvider);

        // Không cần securityMatcher, nó sẽ hứng tất cả những gì Cấu hình 1 bỏ qua

        http.authorizeHttpRequests(auth -> auth
                // Public URLs
                .requestMatchers(
                        "/", "/home", "/login", "/logout", "/register",
                        "/css/**", "/js/**", "/images/**", "/webjars/**",
                        "/tournament/**", "/player/**", "/coach/**", "/team/**",
                        "/stadium/**", "/matches/**", "/blogs/**", "/news/**", "/layout/**","/tournaments-detail",
                        "/oauth2/**"
                ).permitAll()

                // Yêu cầu login
                .requestMatchers("/ticket").authenticated()

                // Các request còn lại
                .anyRequest().permitAll()
        );

        // Form login cho User (Trang /login)
        http.formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/process-login")
                .defaultSuccessUrl("/?success", true)
                .failureHandler(customAuthFailureHandler) // Handler lỗi của User
                .usernameParameter("username")
                .passwordParameter("password")
                .permitAll()
        );

        http.oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
        );

        http.logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
        );

        http.exceptionHandling(ex -> ex.accessDeniedPage("/403"));

        return http.build();
    }
}