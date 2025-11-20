package com.example.premier_league.controller;

import com.example.premier_league.dto.TournamentDto;
import com.example.premier_league.entity.Team;
import com.example.premier_league.entity.Tournament;
import com.example.premier_league.service.ITeamService;
import com.example.premier_league.service.ITournamentService;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin") // Thêm prefix chung cho cả class để gọn code hơn
public class TournamentController {

    private final ITournamentService tournamentService;
    private final ITeamService teamService;

    public TournamentController(ITournamentService tournamentService, ITeamService teamService) {
        this.tournamentService = tournamentService;
        this.teamService = teamService;
    }

    // URL thực tế: /admin/tournaments
    @GetMapping("/tournaments")
    public String list(Model model) {
        List<Tournament> list = tournamentService.findAll();
        model.addAttribute("tournaments", list);
        return "tournament/list";
    }

    // URL thực tế: /admin/tournaments/add
    @GetMapping("/tournaments/add")
    public String addForm(Model model) {
        model.addAttribute("tournamentDto", new TournamentDto());
        return "tournament/add";
    }

    @PostMapping("/tournaments/add")
    public String save(@Valid @ModelAttribute("tournamentDto") TournamentDto tournamentDto,
                       BindingResult bindingResult,
                       Model model) {

        // ... (Giữ nguyên phần validation logic) ...
        // (Code validation ngày tháng & trùng lặp mùa giải...)

        if (bindingResult.hasErrors()) {
            return "tournament/add";
        }

        try {
            Tournament tournament = new Tournament();
            BeanUtils.copyProperties(tournamentDto, tournament);
            tournament.setName("Premier League");
            tournamentService.save(tournament);

            // SỬA REDIRECT: Thêm /admin
            return "redirect:/admin/tournaments";

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Lỗi hệ thống: " + e.getMessage());
            return "tournament/add";
        }
    }

    // URL thực tế: /admin/tournaments/delete/{id}
    @GetMapping("/tournaments/delete/{id}")
    public String delete(@PathVariable Long id) {
        tournamentService.delete(id);
        // SỬA REDIRECT
        return "redirect:/admin/tournaments";
    }

    // URL thực tế: /admin/tournaments/{id}/manage-teams
    @GetMapping("/tournaments/{id}/manage-teams")
    public String showManageTeamsForm(@PathVariable Long id, Model model) {
        Tournament tournament = tournamentService.findById(id);
        if (tournament == null) return "redirect:/admin/tournaments"; // SỬA REDIRECT

        List<Team> allTeams = teamService.findAll();
        List<Tournament> otherTournaments = tournamentService.findAll().stream()
                .filter(t -> !t.getId().equals(id))
                .collect(Collectors.toList());
        boolean isLocked = tournamentService.isTournamentStarted(id);

        model.addAttribute("tournament", tournament);
        model.addAttribute("allTeams", allTeams);
        model.addAttribute("otherTournaments", otherTournaments);
        model.addAttribute("isLocked", isLocked);
        model.addAttribute("tournamentTeamIds",
                tournament.getTeams().stream().map(Team::getId).collect(Collectors.toSet())
        );

        return "tournament/manage-teams";
    }

    @PostMapping("/tournaments/save-teams")
    public String saveTeamsForTournament(@RequestParam("tournamentId") Long tournamentId,
                                         @RequestParam(value = "teamIds", required = false) List<Long> teamIds,
                                         RedirectAttributes redirect) {
        try {
            if (teamIds == null || teamIds.size() != 20) {
                redirect.addFlashAttribute("error", "Lỗi: Giải đấu Premier League bắt buộc phải có đúng 20 đội bóng!");
                // SỬA REDIRECT
                return "redirect:/admin/tournaments/" + tournamentId + "/manage-teams";
            }
            tournamentService.updateTeamsForTournament(tournamentId, teamIds);
            redirect.addFlashAttribute("message", "Cập nhật danh sách 20 đội thành công!");

        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        // SỬA REDIRECT
        return "redirect:/admin/tournaments/" + tournamentId + "/manage-teams";
    }

    @PostMapping("/tournaments/copy-teams")
    public String copyTeams(@RequestParam("targetId") Long targetId,
                            @RequestParam("sourceId") Long sourceId,
                            RedirectAttributes redirect) {
        try {
            tournamentService.copyTeamsFromTournament(targetId, sourceId);
            redirect.addFlashAttribute("message", "Đã sao chép danh sách đội thành công!");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Lỗi sao chép: " + e.getMessage());
        }
        // SỬA REDIRECT
        return "redirect:/admin/tournaments/" + targetId + "/manage-teams";
    }
}