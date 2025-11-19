package com.example.premier_league.controller;

import com.example.premier_league.entity.*;
import com.example.premier_league.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    @ModelAttribute
    public void addCommonAttributes(@PathVariable Long teamId, Model model) {
        Team team = iTeamService.findById(teamId);
        if (team == null) throw new RuntimeException("Không tìm thấy đội");
        model.addAttribute("teamId", teamId);
        model.addAttribute("teamName", team.getName());
    }

    @GetMapping("/schedule")
    public String getMatchSchedule(@PathVariable Long teamId, Model model) {
        model.addAttribute("matchesDto", iMatchScheduleService.getCoachMatchSchedules(teamId));
        model.addAttribute("pageTitle", "Lịch thi đấu & Đội hình");
        model.addAttribute("activePage", "schedule");
        return "coach/match_schedule";
    }

    // ===================== 1. QUẢN LÝ LỊCH TẬP =====================
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

    @PostMapping("/training/save")
    public String saveTrainingSchedule(@PathVariable Long teamId,
                                       @ModelAttribute TrainingSchedule schedule,
                                       RedirectAttributes redirect) {
        try {
            Team team = iTeamService.findById(teamId);
            schedule.setTeam(team);
            iTrainingScheduleService.save(schedule); // Validate ở service
            redirect.addFlashAttribute("message", "Lưu lịch tập thành công!");
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("error", e.getMessage()); // Hiển thị lỗi validate
            return "redirect:/coach/team/" + teamId + "/training/add";
        }
        return "redirect:/coach/team/" + teamId + "/training";
    }

    @GetMapping("/training/delete/{trainingId}")
    public String deleteTrainingSchedule(@PathVariable Long teamId, @PathVariable Long trainingId, RedirectAttributes redirect) {
        iTrainingScheduleService.deleteById(trainingId);
        redirect.addFlashAttribute("message", "Xóa thành công");
        return "redirect:/coach/team/" + teamId + "/training";
    }

    // ===================== 2. DANH SÁCH CẦU THỦ =====================
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

    // ===================== 3. SƠ ĐỒ CHIẾN THUẬT (DRAFT) =====================
    // Cho phép lưu nháp, không ràng buộc số lượng
    @GetMapping("/match/{matchId}/tactics")
    public String showTacticsForm(@PathVariable Long teamId, @PathVariable Long matchId, Model model) {
        prepareLineupModel(teamId, matchId, model);
        model.addAttribute("pageTitle", "Sơ đồ chiến thuật");
        return "coach/manage_lineup"; // File cũ, đổi tên hiển thị thôi
    }

    @PostMapping("/match/{matchId}/tactics/save")
    public String saveTactics(@PathVariable Long teamId,
                              @PathVariable Long matchId,
                              @RequestParam(value = "mainPlayerIds", required = false) List<Long> mainPlayerIds,
                              @RequestParam(value = "subPlayerIds", required = false) List<Long> subPlayerIds,
                              RedirectAttributes redirect) {
        // Gọi service với captainId = null (vì là sơ đồ nháp)
        iMatchLineupService.saveLineup(teamId, matchId, mainPlayerIds, subPlayerIds, null);
        redirect.addFlashAttribute("message", "Đã lưu sơ đồ chiến thuật (Nháp)!");
        return "redirect:/coach/team/" + teamId + "/match/" + matchId + "/tactics";
    }

    // ===================== 4. ĐĂNG KÝ ĐỘI HÌNH (OFFICIAL) =====================
    // Bắt buộc đủ 11 người + Captain
    @GetMapping("/match/{matchId}/register")
    public String showRegisterForm(@PathVariable Long teamId, @PathVariable Long matchId, Model model) {
        prepareLineupModel(teamId, matchId, model);

        // Tìm captain hiện tại
        List<MatchLineup> currentLineup = iMatchLineupService.findByMatchAndTeam(matchId, teamId);
        Long currentCaptainId = currentLineup.stream()
                .filter(MatchLineup::isCaptain)
                .map(l -> l.getPlayer().getId())
                .findFirst().orElse(null);

        model.addAttribute("currentCaptainId", currentCaptainId);
        model.addAttribute("pageTitle", "Đăng ký đội hình");

        // Lấy toàn bộ cầu thủ để hiển thị ở bảng chọn
        model.addAttribute("allPlayersForTable", iPlayerService.findByTeamId(teamId));

        return "coach/register_lineup"; // File mới
    }

    @PostMapping("/match/{matchId}/register/save")
    public String saveRegistration(@PathVariable Long teamId,
                                   @PathVariable Long matchId,
                                   @RequestParam(value = "mainPlayerIds", required = false) List<Long> mainPlayerIds,
                                   @RequestParam(value = "subPlayerIds", required = false) List<Long> subPlayerIds,
                                   @RequestParam(value = "captainId", required = false) Long captainId,
                                   RedirectAttributes redirect) {

        List<Long> mainIds = (mainPlayerIds != null) ? mainPlayerIds : List.of();

        // Validate: Phải đủ 11 người
        if (mainIds.size() != 11) {
            redirect.addFlashAttribute("error", "Đăng ký thất bại: Đội hình chính phải có ĐỦ 11 cầu thủ!");
            return "redirect:/coach/team/" + teamId + "/match/" + matchId + "/register";
        }

        // Validate: Phải có captain
        if (captainId == null) {
            redirect.addFlashAttribute("error", "Đăng ký thất bại: Chưa chọn Đội trưởng!");
            return "redirect:/coach/team/" + teamId + "/match/" + matchId + "/register";
        }

        // Validate: Captain phải đá chính
        if (!mainIds.contains(captainId)) {
            redirect.addFlashAttribute("error", "Đăng ký thất bại: Đội trưởng phải nằm trong danh sách đá chính!");
            return "redirect:/coach/team/" + teamId + "/match/" + matchId + "/register";
        }

        iMatchLineupService.saveLineup(teamId, matchId, mainPlayerIds, subPlayerIds, captainId);
        redirect.addFlashAttribute("message", "Đăng ký đội hình thành công!");
        return "redirect:/coach/team/" + teamId + "/schedule";
    }

    // --- Helper ---
    private void prepareLineupModel(Long teamId, Long matchId, Model model) {
        MatchSchedule match = iMatchScheduleService.findById(matchId);
        List<Player> allPlayers = iPlayerService.findByTeamId(teamId);
        List<MatchLineup> currentLineup = iMatchLineupService.findByMatchAndTeam(matchId, teamId);

        List<Player> mainPlayers = currentLineup.stream().filter(l -> l.getType() == MatchLineup.LineupType.MAIN).map(MatchLineup::getPlayer).collect(Collectors.toList());
        List<Player> subPlayers = currentLineup.stream().filter(l -> l.getType() == MatchLineup.LineupType.SUB).map(MatchLineup::getPlayer).collect(Collectors.toList());
        List<Long> selectedIds = currentLineup.stream().map(l -> l.getPlayer().getId()).collect(Collectors.toList());
        List<Player> availablePlayers = allPlayers.stream().filter(p -> !selectedIds.contains(p.getId())).collect(Collectors.toList());

        model.addAttribute("match", match);
        model.addAttribute("availablePlayers", availablePlayers);
        model.addAttribute("mainPlayers", mainPlayers);
        model.addAttribute("subPlayers", subPlayers);
        model.addAttribute("formations", iFormationService.getAllFormations());
        model.addAttribute("currentFormationName", "4-4-2");
    }
}