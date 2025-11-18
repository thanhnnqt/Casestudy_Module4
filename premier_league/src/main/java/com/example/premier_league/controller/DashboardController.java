package com.example.premier_league.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/admin/owner")
    public String dashboard() {
        return "owner/dashboard";
    }

}
