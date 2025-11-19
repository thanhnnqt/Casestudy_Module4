package com.example.premier_league.controller;

import com.example.premier_league.entity.*;
import com.example.premier_league.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Comparator; // <-- THÊM IMPORT
import java.util.LinkedHashMap; // <-- THÊM IMPORT
import java.util.List;
import java.util.Map; // <-- THÊM IMPORT
import java.util.Objects;
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

    /**
     * Helper
     * Luôn thêm teamId và teamName vào model cho các trang
     */
    @ModelAttribute
    public void addCommonAttributes(@PathVariable Long teamId, Model model) {
        Team team = iTeamService.findById(teamId);
        if (team == null) {
            throw new RuntimeException("Không tìm thấy đội với ID: " + teamId);
        }
        model.addAttribute("teamId", teamId);
        model.addAttribute("teamName", team.getName());
    }

    /**
     * TRANG CHÍNH (REDIRECT TỚI LỊCH THI ĐẤU)
     */
    @GetMapping
    public String coachHome(@PathVariable Long teamId) {
        return "redirect:/coach/team/" + teamId + "/schedule";
    }

    /**
     * Yêu cầu 1: Xem danh sách cầu thủ
     * === ĐÃ CẬP NHẬT LOGIC GROUPING ===
     */
    @GetMapping("/players")
    public String getPlayerList(@PathVariable Long teamId, Model model) {
        List<Player> players = iPlayerService.findByTeamId(teamId);

        // 1. Định nghĩa thứ tự ưu tiên của vị trí
        Comparator<String> positionComparator = (p1, p2) -> {
            Map<String, Integer> order = Map.of("GK", 1, "DF", 2, "MF", 3, "FW", 4);
            // Vị trí lạ sẽ bị đẩy xuống cuối (99)
            return order.getOrDefault(p1, 99) - order.getOrDefault(p2, 99);
        };

        // 2. Group (nhóm) các cầu thủ theo vị trí
        // 3. Sort (sắp xếp) các nhóm theo thứ tự đã định nghĩa (GK -> DF -> MF -> FW)
        // 4. Thu thập kết quả vào một LinkedHashMap để giữ nguyên thứ tự đã sắp xếp
        Map<String, List<Player>> playersByPosition = players.stream()
                .collect(Collectors.groupingBy(Player::getPosition)) // Nhóm theo vị trí
                .entrySet().stream() // Chuyển sang Stream để sắp xếp Map
                .sorted(Map.Entry.comparingByKey(positionComparator)) // Sắp xếp Key (vị trí)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new // Sử dụng LinkedHashMap để duy trì thứ tự
                ));

        // 5. Gửi Map (thay vì List) sang view
        model.addAttribute("playersByPosition", playersByPosition);

        // Thêm các thuộc tính cho layout
        model.addAttribute("pageTitle", "Danh sách cầu thủ");
        model.addAttribute("activePage", "players");
        return "coach/player_list";
    }

    /**
     * Yêu cầu 2: Xem lịch thi đấu (ĐÃ CẬP NHẬT)
     */
    @GetMapping("/schedule")
    public String getMatchSchedule(@PathVariable Long teamId, Model model) {
        // Sử dụng DTO mới để lấy lịch thi đấu KÈM TRẠNG THÁI ĐỘI HÌNH
        model.addAttribute("matchesDto", iMatchScheduleService.getCoachMatchSchedules(teamId));

        // Thêm các thuộc tính cho layout
        model.addAttribute("pageTitle", "Lịch thi đấu & Đội hình");
        model.addAttribute("activePage", "schedule");
        return "coach/match_schedule";
    }

    // =================================================================
    // Yêu cầu 3: CRUD Lịch tập (Training)
    // =================================================================

    /**
     * (READ) Hiển thị danh sách lịch tập
     */
    @GetMapping("/training")
    public String listTrainingSchedules(@PathVariable Long teamId, Model model) {
        model.addAttribute("schedules", iTrainingScheduleService.findByTeamId(teamId));
        // Thêm các thuộc tính cho layout
        model.addAttribute("pageTitle", "Quản lý Lịch tập");
        model.addAttribute("activePage", "training");
        return "coach/training_list";
    }

    /**
     * (CREATE - Form) Hiển thị form thêm mới
     */
    @GetMapping("/training/add")
    public String showAddTrainingForm(Model model) {
        model.addAttribute("schedule", new TrainingSchedule());
        model.addAttribute("formTitle", "Thêm lịch tập mới");
        // Thêm các thuộc tính cho layout
        model.addAttribute("pageTitle", "Thêm Lịch tập");
        model.addAttribute("activePage", "training");
        return "coach/training_form";
    }

    /**
     * (UPDATE - Form) Hiển thị form chỉnh sửa
     */
    @GetMapping("/training/edit/{trainingId}")
    public String showEditTrainingForm(@PathVariable Long teamId, @PathVariable Long trainingId, Model model) {
        TrainingSchedule schedule = iTrainingScheduleService.findById(trainingId);
        // Đảm bảo HLV chỉ sửa được lịch của đội mình
        if (schedule == null || !schedule.getTeam().getId().equals(teamId)) {
            return "redirect:/coach/team/" + teamId + "/training";
        }
        model.addAttribute("schedule", schedule);
        model.addAttribute("formTitle", "Chỉnh sửa lịch tập");
        // Thêm các thuộc tính cho layout
        model.addAttribute("pageTitle", "Sửa Lịch tập");
        model.addAttribute("activePage", "training");
        return "coach/training_form";
    }

    /**
     * (CREATE / UPDATE - Logic) Lưu lịch tập
     */
    @PostMapping("/training/save")
    public String saveTrainingSchedule(@PathVariable Long teamId,
                                       @ModelAttribute TrainingSchedule schedule,
                                       RedirectAttributes redirect) {
        Team team = iTeamService.findById(teamId);
        schedule.setTeam(team); // Quan trọng: Set đội cho lịch tập
        iTrainingScheduleService.save(schedule);

        redirect.addFlashAttribute("message", "Lưu lịch tập thành công!");
        return "redirect:/coach/team/" + teamId + "/training";
    }

    /**
     * (DELETE) Xóa lịch tập
     */
    @GetMapping("/training/delete/{trainingId}")
    public String deleteTrainingSchedule(@PathVariable Long teamId,
                                         @PathVariable Long trainingId,
                                         RedirectAttributes redirect) {
        // Kiểm tra an toàn: Đảm bảo HLV chỉ xóa được lịch của đội mình
        TrainingSchedule schedule = iTrainingScheduleService.findById(trainingId);
        if (schedule != null && schedule.getTeam().getId().equals(teamId)) {
            iTrainingScheduleService.deleteById(trainingId);
            redirect.addFlashAttribute("message", "Xóa lịch tập thành công!");
        } else {
            redirect.addFlashAttribute("error", "Không thể xóa lịch tập này!");
        }
        return "redirect:/coach/team/" + teamId + "/training";
    }

    // =================================================================
    // Yêu cầu 4 & 5: Đăng ký đội hình (Lineup) - (ĐÃ CẬP NHẬT)
    // =================================================================

    /**
     * (READ / UPDATE - Form) Hiển thị form đăng ký đội hình MỚI
     */
    @GetMapping("/match/{matchId}/lineup")
    public String showLineupForm(@PathVariable Long teamId, @PathVariable Long matchId, Model model) {
        MatchSchedule match = iMatchScheduleService.findById(matchId);
        // Kiểm tra HLV có quyền truy cập trận này không
        if (!Objects.equals(match.getHomeTeam().getId(), teamId) && !Objects.equals(match.getAwayTeam().getId(), teamId)) {
            return "redirect:/coach/team/" + teamId + "/schedule";
        }

        List<Player> allPlayers = iPlayerService.findByTeamId(teamId);
        List<MatchLineup> currentLineup = iMatchLineupService.findByMatchAndTeam(matchId, teamId);

        List<Player> mainPlayers = currentLineup.stream()
                .filter(l -> l.getType() == MatchLineup.LineupType.MAIN)
                .map(MatchLineup::getPlayer)
                .collect(Collectors.toList());

        List<Player> subPlayers = currentLineup.stream()
                .filter(l -> l.getType() == MatchLineup.LineupType.SUB)
                .map(MatchLineup::getPlayer)
                .collect(Collectors.toList());

        // Lọc ra những cầu thủ chưa được chọn
        List<Long> selectedIds = currentLineup.stream().map(l -> l.getPlayer().getId()).collect(Collectors.toList());
        List<Player> availablePlayers = allPlayers.stream()
                .filter(p -> !selectedIds.contains(p.getId()))
                .collect(Collectors.toList());

        model.addAttribute("match", match);
        model.addAttribute("availablePlayers", availablePlayers);
        model.addAttribute("mainPlayers", mainPlayers);
        model.addAttribute("subPlayers", subPlayers);

        // === THÊM DỮ LIỆU SƠ ĐỒ ===
        model.addAttribute("formations", iFormationService.getAllFormations());
        model.addAttribute("currentFormationName", "4-4-2"); // Mặc định

        return "coach/manage_lineup";
    }

    /**
     * (CREATE / UPDATE - Logic) Lưu đội hình (ĐÃ CẬP NHẬT RÀNG BUỘC)
     */
    @PostMapping("/match/{matchId}/lineup/save")
    public String saveLineup(@PathVariable Long teamId,
                             @PathVariable Long matchId,
                             @RequestParam(value = "mainPlayerIds", required = false) List<Long> mainPlayerIds,
                             @RequestParam(value = "subPlayerIds", required = false) List<Long> subPlayerIds,
                             RedirectAttributes redirect) {

        // Đảm bảo list không bị null nếu không chọn ai
        List<Long> mainIds = (mainPlayerIds != null) ? mainPlayerIds : List.of();
        List<Long> subIds = (subPlayerIds != null) ? subPlayerIds : List.of();

        // === RÀNG BUỘC MỚI ===
        if (mainIds.size() != 11) {
            redirect.addFlashAttribute("error", "Đội hình chính phải có ĐÚNG 11 cầu thủ!");
            return "redirect:/coach/team/" + teamId + "/match/" + matchId + "/lineup";
        }

        if (subIds.size() > 9) {
            redirect.addFlashAttribute("error", "Đội hình dự bị không được vượt quá 9 cầu thủ!");
            return "redirect:/coach/team/" + teamId + "/match/" + matchId + "/lineup";
        }
        // === HẾT RÀNG BUỘC ===

        iMatchLineupService.saveLineup(teamId, matchId, mainIds, subIds);
        redirect.addFlashAttribute("message", "Đã lưu đội hình thành công!");
        return "redirect:/coach/team/" + teamId + "/schedule";
    }
}