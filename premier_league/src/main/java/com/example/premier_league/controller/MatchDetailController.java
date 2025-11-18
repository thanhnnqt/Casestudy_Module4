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

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {

        Match match = matchService.findById(id);
        List<MatchEvent> events = eventService.getEventsByMatch(id);
        var stats = statsService.getStats(id);

        model.addAttribute("match", match);
        model.addAttribute("events", events);
        model.addAttribute("stats", stats);

        return "match_detail"; // match-detail.html
    }
}
