package com.example.premier_league.config;

import com.example.premier_league.entity.Account;
import com.example.premier_league.repository.IAccountRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Component
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private IAccountRepository accountRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());

        String redirectUrl = "/?success"; // mặc định

        // 🔐 1. ADMIN
        if (roles.contains("ROLE_ADMIN")) {
            redirectUrl = "/admin/home";
        }

        // 🔐 2. COACH
        else if (roles.contains("ROLE_COACH")) {
            String username = authentication.getName();
            Account account = accountRepository.findByUsername(username).orElse(null);

            if (account != null && account.getTeam() != null) {
                redirectUrl = "/coach/team/" + account.getTeam().getId() + "/schedule?success";
            }
        }

        // 🔐 3. OWNER → CHUYỂN TỚI /owner/coaches/{teamId}
        else if (roles.contains("ROLE_OWNER")) {
            String username = authentication.getName();
            Account account = accountRepository.findByUsername(username).orElse(null);

            if (account != null && account.getTeam() != null) {
                Long teamId = account.getTeam().getId();

                // Chuyển đúng vào trang quản lý HLV theo team của chủ tịch
                redirectUrl = "/owner" ;
            }
        }

        response.sendRedirect(redirectUrl);
    }
}
