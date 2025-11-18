package com.example.premier_league.config;

import com.example.premier_league.entity.Account;
import com.example.premier_league.repository.IAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
                    account.getRoles()
                            .stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRoleName()))
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

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationProvider authProvider) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);
        http.authenticationProvider(authProvider);

        http.authorizeHttpRequests(auth -> auth
                // PUBLIC: tất cả trang khác
                .requestMatchers(
                        "/", "/home", "/login", "/logout", "/register",
                        "/css/**", "/js/**", "/images/**", "/webjars/**",
                        "/tournament/**", "/player/**", "/coach/**", "/team/**",
                        "/stadium/**", "/matches/**", "/blogs/**", "/news/**", "/layout/**",
                        "/oauth2/**")
                .permitAll()

                // Chỉ trang đặt vé yêu cầu login
                .requestMatchers("/ticket").authenticated()

                // Còn lại public
                .anyRequest().permitAll()
        );

        // Form login
        http.formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/process-login")
                .defaultSuccessUrl("/", false) // redirect về page gốc nếu trước đó bấm ticket
                .failureHandler(customAuthFailureHandler)
                .usernameParameter("username")
                .passwordParameter("password")
                .permitAll()
        );

        // Google OAuth2 login
        http.oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .defaultSuccessUrl("/", false)
        );

        // Logout
        http.logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
        );

        // 403 Access Denied
        http.exceptionHandling(ex -> ex.accessDeniedPage("/403"));

        return http.build();
    }
}
