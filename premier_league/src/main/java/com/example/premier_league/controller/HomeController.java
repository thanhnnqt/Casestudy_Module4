package com.example.premier_league.controller;

import com.example.premier_league.entity.*;
import com.example.premier_league.repository.IMatchRepository;
import com.example.premier_league.repository.IPlayerRepository;
import com.example.premier_league.repository.ITeamRepository;
import com.example.premier_league.service.IPlayerService;
import com.example.premier_league.service.impl.CoachService;
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
    private final IPlayerService playerService;
    private final CoachService coachService;


    public HomeController(
            MatchScheduleService matchScheduleService,
            ITeamRepository teamRepository,
            IMatchRepository matchRepository,
            IPlayerRepository playerRepository,
            IPlayerService playerService,
            CoachService coachService) {
        this.matchScheduleService = matchScheduleService;
        this.teamRepository = teamRepository;
        this.matchRepository = matchRepository;
        this.playerRepository = playerRepository;
        this.playerService = playerService;
        this.coachService = coachService;
    }

    // 1. Trang Home
    @GetMapping("/")
    public String home(
            @RequestParam(value = "success", required = false) String success,
            @RequestParam(value = "round", required = false, defaultValue = "1") Integer round,
            Model model
    ) {
        // Logic hiển thị thông báo Toast khi Login thành công
        if (success != null) {
            model.addAttribute("toastTitle", "Xin chào!");
            model.addAttribute("toastMessage", "Đăng nhập thành công.");
            model.addAttribute("toastType", "success");
        }

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

    // 3. Trang chi tiết giải đấu
    @GetMapping("/tournament/{id}")
    public String tournamentDetail(@PathVariable("id") String id, Model model) {
        return "home/tournaments-detail";
    }

    // 4. Trang chi tiết đội bóng
    @GetMapping("/team/{id}")
    public String teamDetail(@PathVariable("id") Long id, Model model) {
        Team team = teamRepository.findById(id).orElse(null);

        if (team != null) {
            // Trận đấu
            List<Match> matches = matchRepository.findAll().stream()
                    .filter(m -> m.getHomeTeam().getId().equals(id) || m.getAwayTeam().getId().equals(id))
                    .toList();

            // Cầu thủ
            List<Player> players = playerRepository.findAll().stream()
                    .filter(p -> p.getTeam() != null && p.getTeam().getId().equals(id))
                    .toList();

            // Debug HLV
            List<Coach> coaches = team.getCoaches();
            System.out.println("Coaches size: " + (coaches != null ? coaches.size() : "null"));
            if (coaches != null) {
                coaches.forEach(c -> System.out.println(c.getFullName() + " - " + c.getRole()));
            }

            // Lấy HLV chính từ team
            Coach headCoach = coaches != null ? coaches.stream()
                    .filter(c -> "Head Coach".equalsIgnoreCase(c.getRole()))
                    .findFirst()
                    .orElse(null) : null;

            model.addAttribute("team", team);
            model.addAttribute("matches", matches);
            model.addAttribute("players", players);
            model.addAttribute("headCoach", headCoach);
        }

        return "home/team-detail";
    }

    // 5. Trang chi tiết cầu thủ
    @GetMapping("/player/{id}")
    public String playerDetail(@PathVariable Long id, Model model) {
        Player player = playerService.findById(id);

        if (player == null) {
            model.addAttribute("player", null);
            return "home/player-detail";
        }

        Team team = player.getTeam();
        Card card = player.getCard();

        int age = java.time.Period.between(player.getDob(), java.time.LocalDate.now()).getYears();

        model.addAttribute("player", player);
        model.addAttribute("team", team);
        model.addAttribute("card", card);
        model.addAttribute("age", age); //
        return "home/player-detail";
    }


    // 6. Trang HLV
    @GetMapping("/coach/{id}")
    public String coachDetail(@PathVariable("id") Long id, Model model) {
        return "home/coach-detail";
    }

    // 7. Trang sân vận động
    @GetMapping("/stadium/{id}")
    public String stadiumDetail(@PathVariable("id") Long id, Model model) {
        return "home/stadium-detail";
    }

    // 8. Đặt vé
//    @GetMapping("/ticket")
//    public String tickets(Model model) {
//        return "home/ticket";
//    }

    // 9. Tin tức
    @GetMapping("/new")
    public String news(Model model) {
        return "home/new";
    }

    // 10. Đăng nhập
//    @GetMapping("/login")
//    public String login() {
//        return "home/login";
//    }

    // 11. Chi tiết vòng đấu
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
