package com.example.premier_league.controller;

import com.example.premier_league.entity.Match;
import com.example.premier_league.entity.MatchEvent;
import com.example.premier_league.service.IMatchEventService;
import com.example.premier_league.service.IMatchService;
import com.example.premier_league.service.IMatchStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/live_matches")
public class MatchDetailController {

    private final IMatchService matchService;
    private final IMatchEventService eventService;
    private final IMatchStatsService statsService;

    @GetMapping
    public String formMatchDetail() {
        return "match_detail";
    }

    @GetMapping("/{id}")
    public String viewMatch(@PathVariable Long id, Model model) {
        model.addAttribute("matchId", id);
        return "match_detail";   // file match_detail.html realtime
    }
}
