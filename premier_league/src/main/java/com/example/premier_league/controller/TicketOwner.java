package com.example.premier_league.controller;

import com.example.premier_league.entity.MatchSchedule;
import com.example.premier_league.service.IMatchScheduleService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/ticket")
public class TicketOwner {
    final IMatchScheduleService matchScheduleService;

    public TicketOwner(IMatchScheduleService matchScheduleService) {
        this.matchScheduleService = matchScheduleService;
    }

//    @GetMapping()
//    public String tickets(Model model) {
//        Pageable pageable = PageRequest.of(0, 1000);
//        Page<MatchSchedule> matchSchedulePage = matchScheduleService.getAllMatches(pageable);
//        model.addAttribute("matchSchedulePage", matchSchedulePage);
//        return "home/ticket";
//    }

    @GetMapping()
    public String showTicketPage(Model model,
                                 @RequestParam(defaultValue = "0") int page) {

        int size = 6; // số trận mỗi trang
        Pageable pageable = PageRequest.of(page, size);

        Page<MatchSchedule> matchSchedulePage = matchScheduleService.getAllMatches(pageable);

        int totalPages = matchSchedulePage.getTotalPages();
        int currentPage = page;

        int startPage = Math.max(0, currentPage - 1);
        int endPage = Math.min(totalPages - 1, currentPage + 1);

        model.addAttribute("matchSchedulePage", matchSchedulePage);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        return "home/ticket";
    }

}
