package com.example.premier_league.controller;

import com.example.premier_league.entity.MatchSchedule;
import com.example.premier_league.entity.MatchStatus;
import com.example.premier_league.service.MatchScheduleService;
import com.example.premier_league.service.TeamService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/admin")
public class MatchScheduleController {

    private final MatchScheduleService matchScheduleService;
    private final TeamService teamService;

    public MatchScheduleController(MatchScheduleService matchScheduleService, TeamService teamService) {
        this.matchScheduleService = matchScheduleService;
        this.teamService = teamService;
    }

    /* ================= LIST + SEARCH + PAGINATION ================= */

    @GetMapping("/matches")
    public String listMatches(Model model,
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
            } catch (Exception e) {
                model.addAttribute("message", "Vòng đấu phải là số!");
            }
        }

        /* ================= SEARCH PRIORITY ================= */
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
            model.addAttribute("round", roundValue);
        }
        else {
            matchPage = matchScheduleService.getAllMatches(pageable);
        }


        /* ================= Pagination Logic ================= */
        int totalPages = matchPage.getTotalPages();
        int currentPage = page;

        // hiển thị 1 trang trước - 1 trang sau
        int startPage = Math.max(0, currentPage - 1);
        int endPage = Math.min(totalPages - 1, currentPage + 1);

        model.addAttribute("matches", matchPage.getContent());
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("teamsList", teamService.findAll());

        return "match/list";
    }


    /* ======================== Actions ======================== */

    @GetMapping("/matches/postpone/{id}")
    public String postponeMatch(@PathVariable Long id) {
        matchScheduleService.updateStatus(id, MatchStatus.POSTPONED);
        return "redirect:/admin/matches";
    }

    @PostMapping("/matches/resume/{id}")
    public String resumeMatch(@PathVariable Long id) {
        matchScheduleService.updateStatus(id, MatchStatus.SCHEDULED);
        return "redirect:/admin/matches";
    }

    @GetMapping("/matches/start/{id}")
    public String startMatch(@PathVariable Long id) {
        matchScheduleService.updateStatus(id, MatchStatus.LIVE);
        return "redirect:/admin/matches/live/" + id;
    }

    @GetMapping("/matches/live/{id}")
    public String liveMatch(@PathVariable Long id, Model model) {
        model.addAttribute("match", matchScheduleService.findById(id));
        return "match/live";
    }

    @PostMapping("/matches/finish/{id}")
    public String finishMatch(@PathVariable Long id) {
        matchScheduleService.updateStatus(id, MatchStatus.FINISHED);
        return "redirect:/admin/matches";
    }

    @GetMapping("/matches/reschedule/{id}")
    public String showReschedule(@PathVariable Long id, Model model) {
        model.addAttribute("match", matchScheduleService.findById(id));
        return "match/reschedule";
    }

    @PostMapping("/matches/reschedule/save/{id}")
    public String saveReschedule(@PathVariable Long id,
                                 @RequestParam LocalDate newDate,
                                 @RequestParam String newTime) {
        matchScheduleService.reschedule(id, newDate, newTime);
        return "redirect:/admin/matches";
    }
}
