package com.example.premier_league.controller;

import com.example.premier_league.entity.Account;
import com.example.premier_league.entity.Team;
import com.example.premier_league.service.IAccountService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final IAccountService accountService;

    public GlobalControllerAdvice(IAccountService accountService) {
        this.accountService = accountService;
    }

    @ModelAttribute("loggedInUser")
    public Account getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {
            return null;
        }

        String username = authentication.getName();
        return accountService.findByUsername(username).orElse(null);
    }

    @ModelAttribute("loggedInTeam")
    public Team getCurrentTeam(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {
            return null;
        }

        Account account = accountService.findByUsername(authentication.getName()).orElse(null);
        return account != null ? account.getTeam() : null;
    }
}
