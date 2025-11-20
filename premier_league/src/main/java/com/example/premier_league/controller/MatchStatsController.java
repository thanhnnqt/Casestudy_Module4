package com.example.premier_league.controller;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@Controller
@RequiredArgsConstructor
public class MatchStatsController {

    @GetMapping("/admin/stats/{matchId}")
    public String updateStatsPage(
            @PathVariable Long matchId,
            Model model
    ) {
        model.addAttribute("matchId", matchId);
        return "admin_stats";
    }
}

