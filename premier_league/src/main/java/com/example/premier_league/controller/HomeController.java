package com.example.premier_league.controller;

import com.example.premier_league.entity.MatchSchedule;
import com.example.premier_league.service.MatchScheduleService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.List;

@Controller
public class HomeController {

    private final MatchScheduleService matchScheduleService;

    public HomeController(MatchScheduleService matchScheduleService) {
        this.matchScheduleService = matchScheduleService;
    }

    @GetMapping("/")
    public String home(Model model) {
        // Lấy tất cả trận đấu, sắp xếp theo ngày giờ
        Pageable pageable = PageRequest.of(0, 100);
        List<MatchSchedule> allMatches = matchScheduleService.getAllMatches(pageable).getContent();
        System.out.println("Matches size: " + (allMatches == null ? 0 : allMatches.size()));

        // Lọc vòng 1 (không dùng stream/lambda)
        List<MatchSchedule> round1Matches = new ArrayList<>();
        if (allMatches != null) {
            for (MatchSchedule m : allMatches) {
                if (m != null && m.getRound() != null && m.getRound().intValue() == 1) {
                    round1Matches.add(m);
                }
            }
        }

        model.addAttribute("upcomingMatches", round1Matches);
        return "home/home";
    }

    @GetMapping("/tournament")
    public String listMatchesViews(Model model) {
        Pageable pageable = PageRequest.of(0, 100);
        List<MatchSchedule> allMatches = matchScheduleService.getAllMatches(pageable).getContent();
        model.addAttribute("matches", allMatches);
        return "home/tournaments";
    }

    // 3. Trang chi tiết giải đấu
    @GetMapping("/tournament/{id}")
    public String tournamentDetail(@PathVariable("id") String id, Model model) {
        return "home/tournaments-detail";
    }

    // 4. Trang chi tiết đội bóng
    @GetMapping("/team/{id}")
    public String teamDetail(@PathVariable("id") String id, Model model) {
        return "home/team-detail";
    }

    // 5. Trang chi tiết cầu thủ
    @GetMapping("/player/{id}")
    public String playerDetail(@PathVariable("id") String id, Model model) {
        return "home/player-detail";
    }

    // 6. Trang chi tiết huấn luyện viên
    @GetMapping("/coach/{id}")
    public String coachDetail(@PathVariable("id") String id, Model model) {
        return "home/coach-detail";
    }

    // 7. Trang chi tiết sân vận động
    @GetMapping("/stadium/{id}")
    public String stadiumDetail(@PathVariable("id") String id, Model model) {
        return "home/stadium-detail";
    }

    // 8. Trang đặt vé
    @GetMapping("/ticket")
    public String tickets(Model model) {
        return "home/ticket";
    }

    // 9. Trang tin tức
    @GetMapping("/new")
    public String news(Model model) {
        return "home/new";
    }

    // 10. Trang đăng nhập/đăng ký
    @GetMapping("/login")
    public String login() {
        return "home/login";
    }
}
