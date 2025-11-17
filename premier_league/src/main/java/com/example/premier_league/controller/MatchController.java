package com.example.premier_league.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("")
public class MatchController {
        @GetMapping("/matches")
    public String listMatches() {
        return "matches";
    }
}
