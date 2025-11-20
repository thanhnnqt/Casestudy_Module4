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

@Controller
public class DashboardController {
    private final IMatchScheduleService matchScheduleService;
    private final ITeamService teamService;

    public DashboardController(IMatchScheduleService matchScheduleService, ITeamService teamService) {
        this.matchScheduleService = matchScheduleService;
        this.teamService = teamService;
    }

    @GetMapping("/owner/{teamId}")
    public String dashboard() {
        return "owner/dashboard";
    }
    @GetMapping("owner/matches")
    public String matches(Model model,
                          @RequestParam(required = false) String team,
                          @RequestParam(required = false) LocalDate date,
                          @RequestParam(required = false) String round,
                          @RequestParam(defaultValue = "0") int page) {

        Pageable pageable = PageRequest.of(page, 5);

        Page<MatchSchedule> matchPage;

        Integer roundValue = null;
        if (round != null && !round.isEmpty()) {
            try {
                roundValue = Integer.parseInt(round);
                model.addAttribute("round", round);
            } catch (Exception e) {
                model.addAttribute("message", "Vòng đấu phải là số!");
            }
        }

        if (team != null && !team.isEmpty()) {
            matchPage = matchScheduleService.searchByTeam(team, pageable);
            model.addAttribute("team", team);
        }
        else if (date != null) {
            matchPage = matchScheduleService.searchByDate(date, pageable);
            model.addAttribute("date", date);
        }
        else if (roundValue != null) {
            matchPage = matchScheduleService.searchByRound(roundValue, pageable);
        }
        else {
            matchPage = matchScheduleService.getAllMatches(pageable);
        }

        int totalPages = matchPage.getTotalPages();
        int currentPage = page;

        int startPage = Math.max(0, currentPage - 1);     // 1 trang trước
        int endPage   = Math.min(totalPages - 1, currentPage + 1); // 1 trang sau

        model.addAttribute("matches", matchPage.getContent());
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("teamsList", teamService.findAll());


        return "owner/matches";
    }

}
