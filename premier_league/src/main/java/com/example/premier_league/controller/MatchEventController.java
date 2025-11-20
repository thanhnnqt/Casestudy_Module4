package com.example.premier_league.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MatchEventController {

    @GetMapping("/admin/events")
    public String adminEventsPage(@RequestParam Long matchId, Model model) {
        model.addAttribute("matchId", matchId);
        return "admin_event";
    }



}
