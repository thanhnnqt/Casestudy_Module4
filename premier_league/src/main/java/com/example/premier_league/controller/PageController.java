package com.example.premier_league.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/admin/events")
    public String adminEventsPage() {
        return "admin_event";  // trỏ tới admin-event.html
    }

    @GetMapping("/viewer/live")
    public String viewerLivePage() {
        return "viewer"; // trỏ tới viewer.html
    }
}
