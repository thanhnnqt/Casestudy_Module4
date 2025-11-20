package com.example.premier_league.controller;

import com.example.premier_league.entity.MatchSchedule;
import com.example.premier_league.service.IMatchScheduleService;
import com.example.premier_league.service.ITeamService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
public class DashboardController {
    private final IMatchScheduleService matchScheduleService;
    private final ITeamService teamService;

    public DashboardController(IMatchScheduleService matchScheduleService, ITeamService teamService) {
        this.matchScheduleService = matchScheduleService;
        this.teamService = teamService;
    }

    @GetMapping("/admin/owner")
    public String dashboard() {
        return "owner/dashboard";
    }

    @GetMapping("/admin/owner/matches")
    public String matches(
            @RequestParam(value = "round", required = false, defaultValue = "1") Integer round,
            Model model
    ) {
        Pageable pageable = PageRequest.of(0, 100);
        Page<MatchSchedule> matchPage = matchScheduleService.getAllMatches(pageable);

        List<MatchSchedule> roundMatches = new ArrayList<>();
        for (MatchSchedule m : matchPage.getContent()) {
            if (m != null && m.getRound() != null && m.getRound().equals(round)) {
                roundMatches.add(m);
            }
        }

        model.addAttribute("upcomingMatches", roundMatches);
        model.addAttribute("selectedRound", round);

        return "owner/matches";
    }

}
