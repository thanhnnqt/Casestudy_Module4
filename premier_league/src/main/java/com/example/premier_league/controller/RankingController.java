package com.example.premier_league.controller;

import com.example.premier_league.service.IRankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class RankingController {

    private final IRankingService rankingService;

    @GetMapping("/ranking")
    public String showRankingPage(Model model) {

        model.addAttribute("rankings", rankingService.getRanking());

        return "ranking";
    }
}
