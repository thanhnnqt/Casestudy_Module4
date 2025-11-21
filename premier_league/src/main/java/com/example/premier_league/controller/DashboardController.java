package com.example.premier_league.controller;

import com.example.premier_league.entity.*;
import com.example.premier_league.service.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/owner")
public class DashboardController {

    private final IAccountService accountService;
    private final IPlayerService playerService;
    private final IStaffService staffService;
    private final ICoachService coachService;
    private final IMatchScheduleService matchScheduleService;

    public DashboardController(IAccountService accountService,
                               IPlayerService playerService,
                               IStaffService staffService,
                               ICoachService coachService,
                               IMatchScheduleService matchScheduleService) {
        this.accountService = accountService;
        this.playerService = playerService;
        this.staffService = staffService;
        this.coachService = coachService;
        this.matchScheduleService = matchScheduleService;
    }

    @GetMapping({"", "/dashboard"})
    public String dashboard(Model model, Principal principal) {
        // 1. Lấy Team của Owner đăng nhập
        Account account = accountService.findByUsername(principal.getName()).orElse(null);
        if (account == null || account.getTeam() == null) {
            return "redirect:/login";
        }
        Team myTeam = account.getTeam();

        // 2. Lấy các danh sách dữ liệu liên quan đến Team
        List<Player> players = playerService.findByTeamId(myTeam.getId());
        List<Coach> coaches = coachService.findByTeamId(myTeam.getId());
        List<Staff> staffs = staffService.findByTeamId(myTeam.getId());

        // --- KPI 1: ĐIỂM SỐ & THỨ HẠNG (Dùng field points có sẵn) ---
        model.addAttribute("teamPoints", myTeam.getPoints());
        // Giả sử thứ hạng tạm thời tính toán hoặc fix cứng nếu chưa có logic rank
        model.addAttribute("teamRank", "Top " + (myTeam.getPoints() > 50 ? "4" : "10"));

        // --- KPI 2: HIỆU SỐ BÀN THẮNG (goalsFor - goalsAgainst) ---
        model.addAttribute("goalDiff", myTeam.getGoalDifference());
        model.addAttribute("goalsFor", myTeam.getGoalsFor());
        model.addAttribute("goalsAgainst", myTeam.getGoalsAgainst());

        // --- KPI 3: TỔNG NHÂN SỰ (Cộng dồn 3 list) ---
        int totalPersonnel = players.size() + coaches.size() + staffs.size();
        model.addAttribute("totalPersonnel", totalPersonnel);
        model.addAttribute("playerCount", players.size());
        model.addAttribute("staffCount", coaches.size() + staffs.size());

        // --- KPI 4: THẺ PHẠT (Tính tổng thẻ vàng/đỏ cả mùa) ---
        int totalYellow = players.stream().mapToInt(Player::getSeasonYellowCards).sum();
        int totalRed = players.stream().mapToInt(Player::getRedCards).sum();
        model.addAttribute("totalCards", totalYellow + totalRed);

        // --- CHART: TỈ LỆ THẮNG/HÒA/THUA ---
        // Truyền mảng [Thắng, Hòa, Thua] sang JS
        String matchStatsJson = String.format("[%d, %d, %d]",
                myTeam.getWinCount(), myTeam.getDrawCount(), myTeam.getLoseCount());
        model.addAttribute("matchStatsJson", matchStatsJson);

        // --- LIST: CẦU THỦ CẦN CHÚ Ý (Treo giò hoặc nhiều thẻ) ---
        // Lọc cầu thủ có thẻ vàng > 4 HOẶC đang bị treo giò > 0 trận
        List<Player> warningPlayers = players.stream()
                .filter(p -> p.getSeasonYellowCards() >= 4 || p.getSuspensionMatchesRemaining() > 0)
                .sorted(Comparator.comparingInt(Player::getSuspensionMatchesRemaining).reversed()) // Ưu tiên treo giò lên đầu
                .limit(5)
                .collect(Collectors.toList());
        model.addAttribute("warningPlayers", warningPlayers);

        // --- TRẬN ĐẤU TIẾP THEO ---
        // Logic: Tìm trận UPCOMING của team mình
        List<MatchSchedule> matches = matchScheduleService.findAll(); // Nên dùng findByTeamId nếu có
        MatchSchedule nextMatch = matches.stream()
                .filter(m -> (m.getHomeTeam().getId().equals(myTeam.getId()) || m.getAwayTeam().getId().equals(myTeam.getId())))
                .filter(m -> m.getMatchDate().isAfter(LocalDate.now()) || m.getMatchDate().equals(LocalDate.now()))
                .min(Comparator.comparing(MatchSchedule::getMatchDate)) // Lấy ngày gần nhất
                .orElse(null);

        model.addAttribute("nextMatch", nextMatch);
        model.addAttribute("myTeamId", myTeam.getId()); // Để check sân nhà/khách

        return "owner/dashboard";
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