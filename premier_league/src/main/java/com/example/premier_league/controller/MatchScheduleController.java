package com.example.premier_league.controller;

import com.example.premier_league.entity.Match;
import com.example.premier_league.entity.MatchSchedule;
import com.example.premier_league.entity.MatchStatus;
import com.example.premier_league.service.IMatchScheduleService;
import com.example.premier_league.service.IMatchService;
import com.example.premier_league.service.ITeamService;
import com.example.premier_league.util.MatchScheduleGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Controller quản lý lịch thi đấu và thao tác liên quan.
 * Controller này chỉ điều phối (orchestrate) giữa các service,
 * mọi nghiệp vụ phức tạp được đặt trong service tương ứng.
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class MatchScheduleController {

    private final IMatchScheduleService matchScheduleService;
    private final ITeamService teamService;
    private final MatchScheduleGenerator generator;
    private final IMatchService matchService;

    /* ================= DANH SÁCH + TÌM KIẾM + PHÂN TRANG ================= */

    /**
     * Hiển thị danh sách lịch thi đấu, hỗ trợ tìm theo team, date, round và phân trang.
     */
    @GetMapping("/matches")
    public String listMatches(
            Model model,
            @RequestParam(required = false) String team,
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) String round,
            @RequestParam(defaultValue = "0") int page
    ) {

        Pageable pageable = PageRequest.of(page, 10);

        /* ================== PARSE ROUND ================== */
        Integer roundValue = null;
        if (round != null && !round.isEmpty()) {
            try {
                roundValue = Integer.parseInt(round);
            } catch (NumberFormatException ex) {
                model.addAttribute("message", "Vòng đấu phải là số!");
            }
        }

        /* ================== SEARCH COMBINATION ================== */
        Page<MatchSchedule> matchPage =
                matchScheduleService.search(team, date, roundValue, pageable);

        /* ================== PAGINATION DATA ================== */
        int totalPages = matchPage.getTotalPages();
        int currentPage = page;

        model.addAttribute("matches", matchPage.getContent());
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startPage", Math.max(0, currentPage - 1));
        model.addAttribute("endPage", Math.min(totalPages - 1, currentPage + 1));

        /* ================== RETURN FILTER STATE ================== */
        model.addAttribute("team", team);
        model.addAttribute("date", date);
        model.addAttribute("round", round);
        model.addAttribute("teamsList", teamService.findAll());
        model.addAttribute("hasSchedule", matchScheduleService.hasSchedule());

        return "match/list";
    }

    /* ======================== HÀNH ĐỘNG (Action) ======================== */

    /**
     * Hoãn trận (chuyển trạng thái lịch sang POSTPONED)
     */
    @GetMapping("/matches/postpone/{id}")
    public String postponeMatch(@PathVariable Long id) {
        matchScheduleService.updateStatus(id, MatchStatus.POSTPONED);
        return "redirect:/admin/matches";
    }

    /**
     * Tiếp tục trận đã hoãn (chuyển trạng thái lịch sang SCHEDULED)
     */
    @PostMapping("/matches/resume/{id}")
    public String resumeMatch(@PathVariable Long id) {
        matchScheduleService.updateStatus(id, MatchStatus.SCHEDULED);
        return "redirect:/admin/matches";
    }

    /* =========== BẮT ĐẦU TRẬN (Sync giữa MatchSchedule và Match) =========== */

    /**
     * Bắt đầu trận: cập nhật trạng thái của lịch thành LIVE,
     * đồng thời cập nhật Match.status = LIVE và lưu để WebSocket/Ranking có thể xử lý.
     */
    @GetMapping("/matches/start/{id}")
    public String startMatch(@PathVariable Long id) {
        // 1) Cập nhật trạng thái lịch
        matchScheduleService.updateStatus(id, MatchStatus.LIVE);

        // 2) Đồng bộ trạng thái cho Match entity
        MatchSchedule schedule = matchScheduleService.findById(id);
        Match match = schedule.getMatch();

        if (match != null) {
            match.setStatus(MatchStatus.LIVE);
            matchService.save(match); // matchService.save() chịu trách nhiệm broadcast nếu cần
        }

        return "redirect:/admin/matches/live/" + id;
    }

    /**
     * Trang hiển thị trận đang diễn ra (live)
     */
    @GetMapping("/matches/live/{id}")
    public String liveMatch(@PathVariable Long id, Model model) {
        model.addAttribute("match", matchScheduleService.findById(id));
        return "match/live";
    }

    /* =========== KẾT THÚC TRẬN =========== */

    /**
     * Kết thúc trận: cập nhật lịch + cập nhật match (và matchService sẽ xử lý cập nhật BXH nếu cần).
     * Trả về ResponseEntity để gọi Ajax dễ dàng.
     */
    @PostMapping("/matches/finish/{scheduleId}")
    public ResponseEntity<?> finishMatch(@PathVariable Long scheduleId) {

        MatchSchedule schedule = matchScheduleService.findById(scheduleId);
        if (schedule == null) {
            return ResponseEntity.notFound().build();
        }

        Match match = schedule.getMatch();
        if (match == null) {
            return ResponseEntity.badRequest().body("Match không tồn tại");
        }

        // 1) Cập nhật trạng thái lịch
        matchScheduleService.updateStatus(scheduleId, MatchStatus.FINISHED);

        // 2) Cập nhật trạng thái trận
        match.setStatus(MatchStatus.FINISHED);

        // 3) Lưu match: matchService.save() chịu trách nhiệm cập nhật BXH và broadcast
        matchService.save(match);

        return ResponseEntity.ok("Trận đấu đã kết thúc");
    }

    /* ======================== DỜI LỊCH (RESCHEDULE) ======================== */

    /**
     * Hiển thị form dời lịch
     */
    @GetMapping("/matches/reschedule/{id}")
    public String showReschedule(@PathVariable Long id, Model model) {
        model.addAttribute("match", matchScheduleService.findById(id));
        return "match/reschedule";
    }

    /**
     * Lưu dời lịch: nhận newDate (String) và newTime (String)
     * - Kiểm tra định dạng ngày
     * - Gọi service để validate ràng buộc (khoảng cách giữa các trận...)
     */
    @PostMapping("/matches/reschedule/save/{id}")
    public String saveReschedule(
            @PathVariable Long id,
            @RequestParam("newDate") String newDateStr,
            @RequestParam("newTime") String newTime,
            Model model
    ) {

        try {
            LocalDate newDate = LocalDate.parse(newDateStr); // expects yyyy-MM-dd
            matchScheduleService.reschedule(id, newDate, newTime);

        } catch (DateTimeParseException e) {
            model.addAttribute("match", matchScheduleService.findById(id));
            model.addAttribute("error", "Định dạng ngày không hợp lệ (yyyy-MM-dd)");
            return "match/reschedule";

        } catch (IllegalArgumentException ex) {
            model.addAttribute("match", matchScheduleService.findById(id));
            model.addAttribute("error", ex.getMessage());
            return "match/reschedule";
        }

        return "redirect:/admin/matches";
    }

    /* ======================== TẠO LỊCH ======================== */

    /**
     * Hiển thị form tạo lịch
     */
    @GetMapping("/schedule/create")
    public String showCreateForm(Model model) {
        return "match/create";
    }

    /**
     * Tạo lịch bắt đầu từ startDate (String). Validate: startDate >= today.
     */
    @PostMapping("/schedule/create")
    public String create(@RequestParam("startDate") String startDate, Model model) {

        LocalDate date = LocalDate.parse(startDate);

        if (date.isBefore(LocalDate.now())) {
            model.addAttribute("error", "Ngày bắt đầu mùa giải không được nhỏ hơn ngày hiện tại!");
            return "match/create";
        }

        generator.generateSchedule(date);
        return "redirect:/admin/matches";
    }
}
