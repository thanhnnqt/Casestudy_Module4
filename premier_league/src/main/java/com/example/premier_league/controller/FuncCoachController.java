package com.example.premier_league.controller;

import com.example.premier_league.entity.*;
import com.example.premier_league.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.beans.PropertyEditorSupport;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/coach/team/{teamId}")
@RequiredArgsConstructor
public class FuncCoachController {

    private final IPlayerService iPlayerService;
    private final IMatchScheduleService iMatchScheduleService;
    private final ITrainingScheduleService iTrainingScheduleService;
    private final ITeamService iTeamService;
    private final IMatchLineupService iMatchLineupService;
    private final IFormationService iFormationService;

    // --- Helper: Luôn lấy thông tin đội cho mọi Request ---
    @ModelAttribute
    public void addCommonAttributes(@PathVariable Long teamId, Model model) {
        Team team = iTeamService.findById(teamId);
        if (team == null) throw new RuntimeException("Không tìm thấy đội");
        model.addAttribute("teamId", teamId);
        model.addAttribute("teamName", team.getName());
    }

    // --- 0. TRANG CHỦ HLV (Redirect về lịch thi đấu) ---
    // Xử lý lỗi 404 khi vào /coach/team/1
    @GetMapping("")
    public String coachHome(@PathVariable Long teamId) {
        return "redirect:/coach/team/" + teamId + "/schedule";
    }

    // ===================== 1. LỊCH THI ĐẤU =====================
    @GetMapping("/schedule")
    public String getMatchSchedule(@PathVariable Long teamId, Model model) {
        model.addAttribute("matchesDto", iMatchScheduleService.getCoachMatchSchedules(teamId));
        model.addAttribute("pageTitle", "Lịch thi đấu & Đội hình");
        model.addAttribute("activePage", "schedule");
        return "coach/match_schedule";
    }

    // ===================== 2. QUẢN LÝ LỊCH TẬP =====================
    @GetMapping("/training")
    public String listTrainingSchedules(@PathVariable Long teamId, Model model) {
        model.addAttribute("schedules", iTrainingScheduleService.findByTeamId(teamId));
        model.addAttribute("pageTitle", "Quản lý Lịch tập");
        model.addAttribute("activePage", "training");
        return "coach/training_list";
    }

    @GetMapping("/training/add")
    public String showAddTrainingForm(Model model) {
        model.addAttribute("schedule", new TrainingSchedule());
        model.addAttribute("formTitle", "Thêm lịch tập mới");
        model.addAttribute("pageTitle", "Thêm Lịch tập");
        model.addAttribute("activePage", "training");
        return "coach/training_form";
    }

    // --- [QUAN TRỌNG] Hàm Lưu Lịch Tập (Đã sửa lỗi mất dữ liệu) ---
    @PostMapping("/training/save")
    public String saveTrainingSchedule(@PathVariable Long teamId,
                                       @ModelAttribute TrainingSchedule schedule,
                                       Model model, // Thêm Model để truyền lại dữ liệu
                                       RedirectAttributes redirect) {
        try {
            Team team = iTeamService.findById(teamId);
            schedule.setTeam(team);

            // Validate logic thời gian tại Service
            iTrainingScheduleService.save(schedule);

            redirect.addFlashAttribute("message", "Lưu lịch tập thành công!");
            return "redirect:/coach/team/" + teamId + "/training";
        } catch (RuntimeException e) {
            // KHI CÓ LỖI:
            // 1. Gửi thông báo lỗi ra view
            model.addAttribute("error", e.getMessage());

            model.addAttribute("schedule", schedule);

            // 2. Khôi phục các thuộc tính giao diện (Title, ActivePage)
            String title = (schedule.getId() == null) ? "Thêm lịch tập mới" : "Chỉnh sửa lịch tập";
            model.addAttribute("formTitle", title);
            model.addAttribute("pageTitle", "Quản lý Lịch tập");
            model.addAttribute("activePage", "training");

            // 3. Trả về trực tiếp View (không redirect) để giữ lại dữ liệu người dùng vừa nhập
            return "coach/training_form";
        }
    }

    @GetMapping("/training/edit/{trainingId}")
    public String showEditTrainingForm(@PathVariable Long teamId, @PathVariable Long trainingId, Model model) {
        TrainingSchedule schedule = iTrainingScheduleService.findById(trainingId);
        // Kiểm tra quyền sở hữu
        if (schedule == null || !schedule.getTeam().getId().equals(teamId)) {
            return "redirect:/coach/team/" + teamId + "/training";
        }

        model.addAttribute("schedule", schedule);
        model.addAttribute("formTitle", "Chỉnh sửa lịch tập");
        model.addAttribute("pageTitle", "Sửa Lịch tập");
        model.addAttribute("activePage", "training");
        return "coach/training_form";
    }

    @GetMapping("/training/delete/{trainingId}")
    public String deleteTrainingSchedule(@PathVariable Long teamId, @PathVariable Long trainingId, RedirectAttributes redirect) {
        iTrainingScheduleService.deleteById(trainingId);
        redirect.addFlashAttribute("message", "Xóa thành công");
        return "redirect:/coach/team/" + teamId + "/training";
    }

    // ===================== 3. DANH SÁCH CẦU THỦ =====================
    @GetMapping("/players")
    public String getPlayerList(@PathVariable Long teamId, Model model) {
        List<Player> players = iPlayerService.findByTeamId(teamId);
        Comparator<String> positionComparator = (p1, p2) -> {
            Map<String, Integer> order = Map.of("GK", 1, "DF", 2, "MF", 3, "FW", 4);
            return order.getOrDefault(p1, 99) - order.getOrDefault(p2, 99);
        };
        Map<String, List<Player>> playersByPosition = players.stream()
                .collect(Collectors.groupingBy(Player::getPosition))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey(positionComparator))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        model.addAttribute("playersByPosition", playersByPosition);
        model.addAttribute("pageTitle", "Danh sách cầu thủ");
        model.addAttribute("activePage", "players");
        return "coach/player_list";
    }

    // ===================== 4. SƠ ĐỒ CHIẾN THUẬT (DRAFT) =====================
    @GetMapping("/match/{matchId}/tactics")
    public String showTacticsForm(@PathVariable Long teamId, @PathVariable Long matchId, Model model) {
        prepareLineupModel(teamId, matchId, model);
        model.addAttribute("pageTitle", "Sơ đồ chiến thuật");
        return "coach/manage_lineup";
    }

    @PostMapping("/match/{matchId}/tactics/save")
    public String saveTactics(@PathVariable Long teamId,
                              @PathVariable Long matchId,
                              @RequestParam(value = "mainPlayerIds", required = false) List<Long> mainPlayerIds,
                              @RequestParam(value = "subPlayerIds", required = false) List<Long> subPlayerIds,
                              RedirectAttributes redirect) {

        // --- 1. KIỂM TRA TRẠNG THÁI TRẬN ĐẤU (MỚI) ---
        MatchSchedule match = iMatchScheduleService.findById(matchId);
        if (match == null) {
            redirect.addFlashAttribute("error", "Trận đấu không tồn tại!");
            return "redirect:/coach/team/" + teamId + "/schedule";
        }

        // Chặn nếu trận đấu đã kết thúc hoặc đang đá
        if (match.getStatus() == MatchStatus.FINISHED || match.getStatus() == MatchStatus.LIVE) {
            redirect.addFlashAttribute("error", "Trận đấu đã khóa sổ (Đang đá hoặc Đã kết thúc), không thể thay đổi chiến thuật!");
            return "redirect:/coach/team/" + teamId + "/match/" + matchId + "/tactics";
        }
        // ---------------------------------------------

        // Lưu nháp (captain = null)
        iMatchLineupService.saveLineup(teamId, matchId, mainPlayerIds, subPlayerIds, null);
        redirect.addFlashAttribute("message", "Đã lưu sơ đồ chiến thuật (Nháp)!");
        return "redirect:/coach/team/" + teamId + "/match/" + matchId + "/tactics";
    }

    // ===================== 5. ĐĂNG KÝ ĐỘI HÌNH (OFFICIAL) =====================
    @GetMapping("/match/{matchId}/register")
    public String showRegisterForm(@PathVariable Long teamId, @PathVariable Long matchId, Model model) {
        prepareLineupModel(teamId, matchId, model);

        List<MatchLineup> currentLineup = iMatchLineupService.findByMatchAndTeam(matchId, teamId);
        Long currentCaptainId = currentLineup.stream()
                .filter(MatchLineup::isCaptain)
                .map(l -> l.getPlayer().getId())
                .findFirst().orElse(null);

        // Lấy danh sách ID để restore giao diện (nếu cần thiết cho logic restore)
        List<Long> mainIds = currentLineup.stream()
                .filter(l -> l.getType() == MatchLineup.LineupType.MAIN)
                .map(l -> l.getPlayer().getId()).collect(Collectors.toList());
        List<Long> subIds = currentLineup.stream()
                .filter(l -> l.getType() == MatchLineup.LineupType.SUB)
                .map(l -> l.getPlayer().getId()).collect(Collectors.toList());

        model.addAttribute("currentCaptainId", currentCaptainId);
        model.addAttribute("savedMainIds", mainIds);
        model.addAttribute("savedSubIds", subIds);

        model.addAttribute("pageTitle", "Đăng ký đội hình");
        model.addAttribute("allPlayersForTable", iPlayerService.findByTeamId(teamId));

        return "coach/register_lineup";
    }

    @PostMapping("/match/{matchId}/register/save")
    public String saveRegistration(@PathVariable Long teamId,
                                   @PathVariable Long matchId,
                                   @RequestParam(value = "mainPlayerIds", required = false) List<Long> mainPlayerIds,
                                   @RequestParam(value = "subPlayerIds", required = false) List<Long> subPlayerIds,
                                   @RequestParam(value = "captainId", required = false) Long captainId,
                                   RedirectAttributes redirect) {
// --- 1. KIỂM TRA TRẠNG THÁI TRẬN ĐẤU (MỚI) ---
        MatchSchedule match = iMatchScheduleService.findById(matchId);
        if (match == null) {
            redirect.addFlashAttribute("error", "Trận đấu không tồn tại!");
            return "redirect:/coach/team/" + teamId + "/schedule";
        }

        // Nếu trận đấu đã kết thúc (FINISHED) hoặc đang đá (LIVE), chặn lại ngay
        if (match.getStatus() == MatchStatus.FINISHED || match.getStatus() == MatchStatus.LIVE) {
            redirect.addFlashAttribute("error", "Trận đấu đã kết thúc hoặc đang diễn ra, không thể thay đổi đội hình!");
            // Redirect về lại trang đăng ký để người dùng thấy lỗi, hoặc về lịch thi đấu tùy bạn
            return "redirect:/coach/team/" + teamId + "/match/" + matchId + "/register";
        }
        // ---------------------------------------------
        List<Long> mainIds = (mainPlayerIds != null) ? mainPlayerIds : new ArrayList<>();
        List<Long> subIds = (subPlayerIds != null) ? subPlayerIds : new ArrayList<>();

        // Validate số lượng chính
        if (mainIds.size() != 11) {
            redirect.addFlashAttribute("error", "Đăng ký thất bại: Phải chọn ĐỦ 11 cầu thủ đá chính!");
            return "redirect:/coach/team/" + teamId + "/match/" + matchId + "/register";
        }

        // Validate số lượng dự bị
        if (subIds.isEmpty()) {
            redirect.addFlashAttribute("error", "Đăng ký thất bại: Phải có tối thiểu 1 cầu thủ dự bị!");
            return "redirect:/coach/team/" + teamId + "/match/" + matchId + "/register";
        }

        // Validate đội trưởng
        if (captainId == null) {
            redirect.addFlashAttribute("error", "Đăng ký thất bại: Chưa chọn Đội trưởng!");
            return "redirect:/coach/team/" + teamId + "/match/" + matchId + "/register";
        }
        if (!mainIds.contains(captainId)) {
            redirect.addFlashAttribute("error", "Đăng ký thất bại: Đội trưởng phải nằm trong danh sách đá chính!");
            return "redirect:/coach/team/" + teamId + "/match/" + matchId + "/register";
        }

        iMatchLineupService.saveLineup(teamId, matchId, mainIds, subIds, captainId);

        redirect.addFlashAttribute("message", "Đăng ký đội hình thành công!");
        return "redirect:/coach/team/" + teamId + "/schedule";
    }

    // --- Helper Methods ---
    private void prepareLineupModel(Long teamId, Long matchId, Model model) {
        MatchSchedule match = iMatchScheduleService.findById(matchId);
        List<Player> allPlayers = iPlayerService.findByTeamId(teamId);
        List<MatchLineup> currentLineup = iMatchLineupService.findByMatchAndTeam(matchId, teamId);

        List<Player> mainPlayers = currentLineup.stream()
                .filter(l -> l.getType() == MatchLineup.LineupType.MAIN)
                .map(MatchLineup::getPlayer).collect(Collectors.toList());

        List<Player> subPlayers = currentLineup.stream()
                .filter(l -> l.getType() == MatchLineup.LineupType.SUB)
                .map(MatchLineup::getPlayer).collect(Collectors.toList());

        List<Long> selectedIds = currentLineup.stream()
                .map(l -> l.getPlayer().getId()).collect(Collectors.toList());

        List<Player> availablePlayers = allPlayers.stream()
                .filter(p -> !selectedIds.contains(p.getId())).collect(Collectors.toList());

        model.addAttribute("match", match);
        model.addAttribute("availablePlayers", availablePlayers);
        model.addAttribute("mainPlayers", mainPlayers);
        model.addAttribute("subPlayers", subPlayers);
        model.addAttribute("formations", iFormationService.getAllFormations());
        model.addAttribute("currentFormationName", "4-4-2");
    }

    // --- [THÊM MỚI] Cấu hình binding cho LocalDateTime ---
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(LocalDateTime.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                if (text != null && !text.isEmpty()) {
                    setValue(LocalDateTime.parse(text, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                }
            }

            @Override
            public String getAsText() {
                LocalDateTime value = (LocalDateTime) getValue();
                return (value != null ? value.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "");
            }
        });
    }
}