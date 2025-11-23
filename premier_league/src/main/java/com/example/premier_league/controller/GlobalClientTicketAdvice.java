package com.example.premier_league.controller;

import com.example.premier_league.entity.Account;
import com.example.premier_league.service.impl.AccountService;
import com.example.premier_league.service.impl.ClientTicketService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@Component
@ControllerAdvice
public class GlobalClientTicketAdvice {
    private final ClientTicketService clientTicketService;
    private final AccountService accountService;

    public GlobalClientTicketAdvice(ClientTicketService clientTicketService,
                                    AccountService accountService) {
        this.clientTicketService = clientTicketService;
        this.accountService = accountService;
    }

    @ModelAttribute("clientTicketCount")
    public Integer clientTicketCount() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return 0; // chưa đăng nhập -> 0 vé
        }

        String username = auth.getName();
        Account account = accountService.findByUsername(username).orElse(null);

        if (account == null) return 0;

        return clientTicketService.countTicketsByAccount(account);
    }
}
