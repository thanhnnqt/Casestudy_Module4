package com.example.premier_league.controller;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/admin/stats")
@RequiredArgsConstructor
public class MatchStatsController {

    @GetMapping
    public String formUpdateStats(){
        return "admin_stats";
    }
}

