package com.example.premier_league.controller;

import com.example.premier_league.entity.*;
import com.example.premier_league.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/coach/team/{teamId}")
@RequiredArgsConstructor
public class CoachController {

    private final IPlayerService iPlayerService;
    private final IMatchScheduleService iMatchScheduleService;
    private final ITrainingScheduleService iTrainingScheduleService;
    private final ITeamService iTeamService;
    private final IMatchLineupService iMatchLineupService;

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
     * Yêu cầu 1: Xem danh sách cầu thủ
     */
    @GetMapping("/players")
    public String getPlayerList(@PathVariable Long teamId, Model model) {
        model.addAttribute("players", iPlayerService.findByTeamId(teamId));
        return "coach/player_list";
    }

    /**
     * Yêu cầu 2: Xem lịch thi đấu
     */
    @GetMapping("/schedule")
    public String getMatchSchedule(@PathVariable Long teamId, Model model) {
        model.addAttribute("matches", iMatchScheduleService.findMatchesByTeamId(teamId));
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
        return "coach/training_list";
    }

    /**
     * (CREATE - Form) Hiển thị form thêm mới
     */
    @GetMapping("/training/add")
    public String showAddTrainingForm(Model model) {
        model.addAttribute("schedule", new TrainingSchedule());
        model.addAttribute("formTitle", "Thêm lịch tập mới");
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
    // Yêu cầu 4: Đăng ký đội hình (Lineup)
    // =================================================================

    /**
     * (READ / UPDATE - Form) Hiển thị form đăng ký đội hình
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
                .filter(l -> l.getType() == MatchLineup.LineupType.MAIN) // <-- ĐÃ SỬA LỖI Ở ĐÂY
                .map(MatchLineup::getPlayer)
                .collect(Collectors.toList());

        List<Player> subPlayers = currentLineup.stream()
                .filter(l -> l.getType() == MatchLineup.LineupType.SUB) // <-- ĐÃ SỬA LỖI Ở ĐÂY
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

        return "coach/manage_lineup";
    }

    /**
     * (CREATE / UPDATE - Logic) Lưu đội hình
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

        if (mainIds.size() > 11) {
            redirect.addFlashAttribute("error", "Đội hình chính không được vượt quá 11 cầu thủ!");
            return "redirect:/coach/team/" + teamId + "/match/" + matchId + "/lineup";
        }

        iMatchLineupService.saveLineup(teamId, matchId, mainIds, subIds);
        redirect.addFlashAttribute("message", "Đã lưu đội hình thành công!");
        return "redirect:/coach/team/" + teamId + "/schedule";
    }
}