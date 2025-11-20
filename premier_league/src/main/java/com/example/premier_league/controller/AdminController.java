package com.example.premier_league.controller;

import com.example.premier_league.entity.MatchSchedule;
import com.example.premier_league.service.impl.MatchScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
public class AdminController {

    private final MatchScheduleService matchScheduleService;

    // Inject Service qua Constructor
    @Autowired
    public AdminController(MatchScheduleService matchScheduleService) {
        this.matchScheduleService = matchScheduleService;
    }

    // Trang đăng nhập admin
    @GetMapping("/admin/login")
    public String adminLogin() {
        return "admin/admin-login";
    }

    // Trang Dashboard (Tổng quan + Lịch thi đấu)
    @GetMapping("/admin/home")
    public String adminDashboard(
            @RequestParam(value = "round", required = false, defaultValue = "1") Integer round,
            Model model
    ) {
        // 1. Lấy dữ liệu trận đấu (Logic từ tournamentsDetail chuyển sang)
        Pageable pageable = PageRequest.of(0, 100); // Lấy 100 trận (hoặc chỉnh số lớn hơn để lấy hết)
        Page<MatchSchedule> matchPage = matchScheduleService.getAllMatches(pageable);

        // 2. Lọc theo vòng đấu (Logic Java của bạn)
        List<MatchSchedule> roundMatches = new ArrayList<>();
        if (matchPage != null && matchPage.hasContent()) {
            for (MatchSchedule m : matchPage.getContent()) {
                if (m != null && m.getRound() != null && m.getRound().equals(round)) {
                    roundMatches.add(m);
                }
            }
        }

        // 3. Đẩy dữ liệu ra View (admin/admin-home.html)
        model.addAttribute("upcomingMatches", roundMatches);
        model.addAttribute("selectedRound", round);

        return "admin/admin-home";
    }
}