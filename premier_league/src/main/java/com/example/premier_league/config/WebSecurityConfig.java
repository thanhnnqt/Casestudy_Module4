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

    // 1. TIÊM HANDLER TÙY CHỈNH VÀO ĐÂY
    @Autowired
    private CustomLoginSuccessHandler customLoginSuccessHandler;

    @Autowired
    private IAccountRepository accountRepository;

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
    // CẤU HÌNH 1: DÀNH RIÊNG CHO ADMIN (Giữ nguyên)
    // ==================================================================
    @Bean
    @Order(1)
    public SecurityFilterChain adminFilterChain(HttpSecurity http, AuthenticationProvider authProvider) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);
        http.securityMatcher("/admin/**");
        http.authenticationProvider(authProvider);

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin/login", "/admin/process-login", "/admin/logout").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
        );

        http.formLogin(form -> form
                .loginPage("/admin/login")
                .loginProcessingUrl("/admin/process-login")
                .defaultSuccessUrl("/admin/home", true) // Admin login ở trang admin thì về home admin
                .failureUrl("/admin/login?error")
                .usernameParameter("username")
                .passwordParameter("password")
                .permitAll()
        );

        http.logout(logout -> logout
                .logoutUrl("/admin/logout")
                .logoutSuccessUrl("/admin/login?logout")
                .deleteCookies("JSESSIONID")
                .permitAll()
        );

        return http.build();
    }

    // ==================================================================
    // CẤU HÌNH 2: DÀNH CHO USER, COACH, OWNER (Sửa đổi ở đây)
    // ==================================================================
    @Bean
    @Order(2)
    public SecurityFilterChain userFilterChain(HttpSecurity http, AuthenticationProvider authProvider) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);
        http.authenticationProvider(authProvider);

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/", "/home", "/login", "/logout", "/register",
                        "/css/**", "/js/**", "/images/**", "/webjars/**",
                        "/tournament/**", "/player/**", "/coach/**", "/team/**",
                        "/stadium/**", "/matches/**", "/blogs/**", "/news/**", "/layout/**","/tournaments-detail",
                        "/oauth2/**"
                ).permitAll()
                .requestMatchers("/coach/**").hasRole("COACH")
                .requestMatchers("/owner/**").hasAnyRole("ADMIN", "OWNER")
                .requestMatchers("/ticket").authenticated()
                .anyRequest().permitAll()
        );

        // Form login cho User (Trang /login)
        http.formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/process-login")

                // 2. THAY THẾ defaultSuccessUrl BẰNG successHandler
                // .defaultSuccessUrl("/?success", true)  <-- XÓA DÒNG NÀY
                .successHandler(customLoginSuccessHandler) // <-- THÊM DÒNG NÀY

                .failureHandler(customAuthFailureHandler)
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