package com.example.premier_league.controller;

import com.example.premier_league.entity.Account;
import com.example.premier_league.entity.ClientTicket;
import com.example.premier_league.service.IAccountService;
import com.example.premier_league.service.impl.IClientTicketService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/client/tickets")
public class ClientTicketController {
    private final IClientTicketService clientTicketService;
    private final IAccountService accountService;

    public ClientTicketController(IClientTicketService clientTicketService,
                                  IAccountService accountService) {
        this.clientTicketService = clientTicketService;
        this.accountService = accountService;
    }

    @GetMapping
    public String myTickets(Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Account account = accountService.findByUsername(username).orElse(null);
        if (account == null) {
            return "redirect:/login";
        }

        List<ClientTicket> tickets = clientTicketService.findByAccount(account);
        model.addAttribute("tickets", tickets);
        model.addAttribute("ticketCount", tickets.size());

        return "ticket/clientTicketList"; // tạo file html bên dưới
    }
}
