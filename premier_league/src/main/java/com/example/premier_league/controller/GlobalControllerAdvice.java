package com.example.premier_league.controller; // Hoặc package config tùy bạn

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

    // Hàm này sẽ tự động chạy trước mọi Request và nhét dữ liệu vào Model
    @ModelAttribute("loggedInUser")
    public Account getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return null;
        }
        // Lấy username từ Security
        String username = authentication.getName();
        // Tìm account trong DB
        return accountService.findByUsername(username).orElse(null);
    }

    @ModelAttribute("loggedInTeam")
    public Team getCurrentTeam(@ModelAttribute("loggedInUser") Account account) {
        if (account != null) {
            return account.getTeam();
        }
        return null;
    }
}