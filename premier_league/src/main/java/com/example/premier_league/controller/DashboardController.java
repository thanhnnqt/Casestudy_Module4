package com.example.premier_league.controller;

import com.example.premier_league.entity.Account;
import com.example.premier_league.entity.MatchSchedule;
import com.example.premier_league.entity.Team;
import com.example.premier_league.service.IAccountService;
import com.example.premier_league.service.IMatchScheduleService;
import com.example.premier_league.service.IPlayerService;
import com.example.premier_league.service.IStaffService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin/owner")
public class DashboardController {

    private final IMatchScheduleService matchScheduleService;
    private final IAccountService accountService;
    private final IPlayerService playerService;
    private final IStaffService staffService;

    public DashboardController(IMatchScheduleService matchScheduleService,
                               IAccountService accountService,
                               IPlayerService playerService,
                               IStaffService staffService) {
        this.matchScheduleService = matchScheduleService;
        this.accountService = accountService;
        this.playerService = playerService;
        this.staffService = staffService;
    }

    // Trang Dashboard chính
    @GetMapping({"", "/dashboard"})
    public String dashboard(Model model, Principal principal) {
        // 1. Lấy thông tin Owner đang đăng nhập
        String username = principal.getName();
        Account account = accountService.findByUsername(username).orElse(null);

        if (account != null && account.getTeam() != null) {
            Team myTeam = account.getTeam();

            // 2. Gửi thông tin đội bóng sang View
            model.addAttribute("team", myTeam);

            // 3. Tính toán KPI (Dữ liệu thật)
            // Đếm số cầu thủ của đội
            int totalPlayers = playerService.findByTeamId(myTeam.getId()).size();
            // Đếm số nhân viên của đội
            int totalStaffs = staffService.findByTeamId(myTeam.getId()).size();

            model.addAttribute("totalPlayers", totalPlayers);
            model.addAttribute("totalStaffs", totalStaffs);
            // Có thể thêm các chỉ số khác như: Giá trị đội hình, Điểm số...
        }

        // Active menu sidebar
        model.addAttribute("activeMenu", "dashboard");
        model.addAttribute("pageTitle", "Tổng quan CLB");

        return "owner/dashboard"; // File HTML Dashboard đẹp bạn đã có
    }

    // Trang Lịch thi đấu của Owner
    @GetMapping("/matches")
    public String matches(
            @RequestParam(value = "round", required = false, defaultValue = "1") Integer round,
            Model model,
            Principal principal
    ) {
        // Active menu sidebar
        model.addAttribute("activeMenu", "matches");
        model.addAttribute("pageTitle", "Lịch thi đấu");

        Pageable pageable = PageRequest.of(0, 100);
        Page<MatchSchedule> matchPage = matchScheduleService.getAllMatches(pageable);

        // Lọc trận đấu theo vòng
        List<MatchSchedule> roundMatches = new ArrayList<>();
        for (MatchSchedule m : matchPage.getContent()) {
            if (m != null && m.getRound() != null && m.getRound().equals(round)) {
                roundMatches.add(m);
            }
        }

        // Có thể lọc thêm: Chỉ hiện trận đấu của đội mình (nếu cần)
        // String username = principal.getName(); ...

        model.addAttribute("upcomingMatches", roundMatches);
        model.addAttribute("selectedRound", round);

        return "owner/matches";
    }
}