package com.example.premier_league.controller;

import com.example.premier_league.entity.MatchSchedule;
import com.example.premier_league.entity.MatchStatus;
import com.example.premier_league.serivce.MatchScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Controller
@RequestMapping("")
public class MatchScheduleController {

    private final MatchScheduleService matchScheduleService;

    public MatchScheduleController(MatchScheduleService matchScheduleService) {
        this.matchScheduleService = matchScheduleService;
    }

//    @GetMapping("/matches")
//    public String listMatches(Model model) {
//        model.addAttribute("matches", matchScheduleService.getAllMatches());
//        return "match/list";
//    }
    @GetMapping("/matches")
    public String listMatches(Model model) {
        model.addAttribute("matches", matchScheduleService.getAllMatches());
        return "matches";
    }

    @GetMapping("/matches/postpone/{id}")
    public String postponeMatch(@PathVariable Long id) {
        MatchSchedule match = matchScheduleService.findById(id);
        match.setStatus(MatchStatus.POSTPONED);
        matchScheduleService.save(match);
        return "redirect:/matches";
    }
    @GetMapping("/matches/reschedule/{id}")
    public String showRescheduleForm(@PathVariable Long id, Model model) {
        model.addAttribute("match", matchScheduleService.findById(id));
        return "match/reschedule";
    }

    // CẬP NHẬT LỊCH MỚI
    @PostMapping("/matches/reschedule/save/{id}")
    public String rescheduleMatch(@PathVariable Long id,
                                  @RequestParam LocalDate newDate,
                                  @RequestParam LocalTime newTime) {

        MatchSchedule match = matchScheduleService.findById(id);
        match.setMatchDate(newDate);
        match.setMatchTime(newTime);
        match.setStatus(MatchStatus.SCHEDULED); // Đặt lại thành sắp diễn ra

        matchScheduleService.save(match);
        return "redirect:/matches";
    }
    @PostMapping("/matches/resume/{id}")
    public String resumeMatch(@PathVariable Long id) {
        matchScheduleService.resumeMatch(id);
        return "redirect:/matches";
    }
}
