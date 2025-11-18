package com.example.premier_league.controller;

import com.example.premier_league.entity.MatchSchedule;
import com.example.premier_league.service.impl.MatchScheduleService;
import org.springframework.data.domain.Page;
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
//    @GetMapping("/")
//    public String home(Model model) {
//        System.out.println("Matches size: " + matchScheduleService.getAllMatches().size());
//
//        List<MatchSchedule> round1Matches = matchScheduleService.getAllMatches().stream()
//                .filter(m -> m.getRound() == 1)
//                .collect(Collectors.toList());
//        model.addAttribute("upcomingMatches", round1Matches);
//        return "home/home";
//
//    }
//
////     2. Trang danh sách giải đấu
//    @GetMapping("/tournament")
//    public String listMatchesViews(Model model) {
//        model.addAttribute("matches", matchScheduleService.getAllMatches());
//        return "home/tournaments";
//    }
@GetMapping("/")
public String home(Model model) {
    Pageable pageable = PageRequest.of(0, 100);
    Page<MatchSchedule> matchPage = matchScheduleService.getAllMatches(pageable);

    // Lọc vòng 1
    List<MatchSchedule> round1Matches = new ArrayList<>();
    for (MatchSchedule m : matchPage.getContent()) {
        if (m != null && m.getRound() != null && m.getRound() == 1) {
            round1Matches.add(m);
        }
    }

    model.addAttribute("upcomingMatches", round1Matches);
    return "home/home";
}

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
        // TODO: Tìm giải đấu theo id -> thêm vào model
        // TODO: Thêm sortedTeams, upcomingMatches, finishedMatches
        return "home/tournaments-detail";
    }

    // 4. Trang chi tiết đội bóng
    @GetMapping("/team/{id}")
    public String teamDetail(@PathVariable("id") String id, Model model) {
        // TODO: Tìm team theo id -> thêm vào model
        // TODO: Lọc matches của team -> thêm vào model
        return "home/team-detail";
    }

    // 5. Trang chi tiết cầu thủ
    @GetMapping("/player/{id}")
    public String playerDetail(@PathVariable("id") String id, Model model) {
        // TODO: Tìm player theo id -> thêm vào model
        // TODO: Tìm team của player này -> thêm vào model
        return "home/player-detail";
    }

    // 6. Trang chi tiết huấn luyện viên
    @GetMapping("/coach/{id}")
    public String coachDetail(@PathVariable("id") String id, Model model) {
        // TODO: Tìm coach theo id -> thêm vào model
        // TODO: Tìm team mà coach này dẫn dắt -> thêm vào model
        return "home/coach-detail";
    }

    // 7. Trang chi tiết sân vận động
    @GetMapping("/stadium/{id}")
    public String stadiumDetail(@PathVariable("id") String id, Model model) {
        // TODO: Tìm stadium theo id -> thêm vào model
        // TODO: Tìm team sở hữu sân này -> thêm vào model
        return "home/stadium-detail";
    }

    // 8. Trang đặt vé
    @GetMapping("/ticket")
    public String tickets(Model model) {
        // TODO: Thêm dữ liệu: upcomingMatches (để hiển thị list chọn trận)
        return "home/ticket";
    }

    // 9. Trang tin tức
    @GetMapping("/new")
    public String news(Model model) {
        // TODO: Thêm dữ liệu: newsList (List tin tức)
        return "home/new";
    }

    // 10. Trang đăng nhập/đăng ký
    @GetMapping("/login")
    public String login() {
        return "home/login";
    }
}