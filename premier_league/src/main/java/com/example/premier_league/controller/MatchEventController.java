package com.example.premier_league.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MatchEventController {

    @GetMapping("/admin/events")
    public String adminEventsPage() {
        return "admin_event";
    }

}
