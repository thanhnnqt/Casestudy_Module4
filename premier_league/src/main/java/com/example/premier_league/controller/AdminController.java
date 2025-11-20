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

    @Autowired
    private MatchScheduleService matchScheduleService;

    // CẬP NHẬT: Thêm tham số error và logout để hứng thông báo từ Security
    @GetMapping("/admin/login")
    public String adminLogin(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model
    ) {
        if (error != null) {
            model.addAttribute("error", "Tài khoản hoặc mật khẩu quản trị không đúng!");
        }
        if (logout != null) {
            model.addAttribute("message", "Bạn đã đăng xuất khỏi quyền quản trị.");
        }
        return "admin/admin-login";
    }

    @GetMapping("/admin/home") // Lưu ý: Trong WebSecurityConfig bạn để defaultSuccessUrl là /admin/dashboard, hãy chắc chắn đường dẫn khớp nhau (home hay dashboard)
    public String adminDashboard(
            @RequestParam(value = "round", required = false, defaultValue = "1") Integer round,
            Model model
    ) {
        // 1. Lấy toàn bộ trận đấu (Lấy max 380 trận cho cả mùa giải)
        Pageable pageable = PageRequest.of(0, 380);
        Page<MatchSchedule> matchPage = matchScheduleService.getAllMatches(pageable);

        // 2. Lọc danh sách theo vòng (round)
        List<MatchSchedule> roundMatches = new ArrayList<>();
        if (matchPage != null && matchPage.hasContent()) {
            for (MatchSchedule m : matchPage.getContent()) {
                // Kiểm tra null an toàn
                if (m != null && m.getRound() != null && m.getRound().equals(round)) {
                    roundMatches.add(m);
                }
            }
        }

        // 3. Đẩy dữ liệu ra View
        model.addAttribute("upcomingMatches", roundMatches);
        model.addAttribute("selectedRound", round);

        return "admin/admin-home";
    }
}