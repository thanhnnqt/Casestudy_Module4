package com.example.premier_league.controller;

import com.example.premier_league.entity.Match;
import com.example.premier_league.entity.MatchSchedule;
import com.example.premier_league.entity.Player;
import com.example.premier_league.entity.Team;
import com.example.premier_league.repository.IMatchRepository;
import com.example.premier_league.repository.IPlayerRepository;
import com.example.premier_league.repository.ITeamRepository;
import com.example.premier_league.service.impl.MatchScheduleService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
public class HomeController {

    private final MatchScheduleService matchScheduleService;
    private final ITeamRepository teamRepository;
    private final IMatchRepository matchRepository;
    private final IPlayerRepository playerRepository;

    public HomeController(MatchScheduleService matchScheduleService,
                          ITeamRepository teamRepository,
                          IMatchRepository matchRepository,
                          IPlayerRepository playerRepository) {
        this.matchScheduleService = matchScheduleService;
        this.teamRepository = teamRepository;
        this.matchRepository = matchRepository;
        this.playerRepository = playerRepository;
    }


    // 1. Trang Home
    @GetMapping("/")
    public String home(
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

        return "home/home";
    }

    // 2. Trang danh sách giải đấu
    @GetMapping("/tournament")
    public String listMatchesViews(Model model) {
        Pageable pageable = PageRequest.of(0, 100);
        Page<MatchSchedule> matchPage = matchScheduleService.getAllMatches(pageable);
        model.addAttribute("matches", matchPage.getContent());
        return "home/tournaments";
    }

    // 3. Trang chi tiết giải đấu (TODO: Cập nhật theo giải đấu cụ thể)
    @GetMapping("/tournament/{id}")
    public String tournamentDetail(@PathVariable("id") String id, Model model) {
        // TODO: Lấy giải đấu, sortedTeams, upcomingMatches, finishedMatches
        return "home/tournaments-detail";
    }

    // 4. Trang chi tiết đội bóng
    @GetMapping("/team/{id}")
    public String teamDetail(@PathVariable("id") Long id, Model model) {
        Team team = teamRepository.findById(id).orElse(null);
        if (team != null) {
            // Lấy danh sách trận đấu của đội
            List<Match> matches = matchRepository.findAll().stream()
                    .filter(m -> m.getHomeTeam().getId().equals(id) || m.getAwayTeam().getId().equals(id))
                    .toList();

            List<Player> players = playerRepository.findAll().stream()
                    .filter(p -> p.getTeam() != null && p.getTeam().getId().equals(id))
                    .toList();

            model.addAttribute("team", team);
            model.addAttribute("matches", matches);
            model.addAttribute("players", players); // thêm players vào model
        }
        return "home/team-detail";
    }


    // 5. Trang chi tiết cầu thủ
    @GetMapping("/player/{id}")
    public String playerDetail(@PathVariable("id") Long id, Model model) {
        // TODO: Lấy player và team
        return "home/player-detail";
    }

    // 6. Trang chi tiết huấn luyện viên
    @GetMapping("/coach/{id}")
    public String coachDetail(@PathVariable("id") Long id, Model model) {
        // TODO: Lấy coach và team
        return "home/coach-detail";
    }

    // 7. Trang chi tiết sân vận động
    @GetMapping("/stadium/{id}")
    public String stadiumDetail(@PathVariable("id") Long id, Model model) {
        // TODO: Lấy stadium và team
        return "home/stadium-detail";
    }

    // 8. Trang đặt vé
    @GetMapping("/ticket")
    public String tickets(Model model) {
        // TODO: Thêm upcomingMatches để chọn trận
        return "home/ticket";
    }

    // 9. Trang tin tức
    @GetMapping("/new")
    public String news(Model model) {
        // TODO: Thêm newsList
        return "home/new";
    }

    // 10. Trang đăng nhập/đăng ký
    @GetMapping("/login")
    public String login() {
        return "home/login";
    }

    // 11. Trang chi tiết vòng đấu / lọc vòng
    @GetMapping("/tournaments-detail")
    public String tournamentsDetail(
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

        return "home/tournaments-detail";
    }

}
