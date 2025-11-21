package com.example.premier_league.controller;

import com.example.premier_league.entity.MatchSchedule;
import com.example.premier_league.service.IMatchScheduleService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/ticket")

public class TicketOwner {
    final IMatchScheduleService matchScheduleService;

    public TicketOwner(IMatchScheduleService matchScheduleService) {
        this.matchScheduleService = matchScheduleService;
    }

    @GetMapping()
    public String tickets(Model model) {
        Pageable pageable = PageRequest.of(0, 1000);
        Page<MatchSchedule> matchSchedulePage = matchScheduleService.getAllMatches(pageable);
        model.addAttribute("matchSchedulePage", matchSchedulePage);
        return "home/ticket";
    }
}
