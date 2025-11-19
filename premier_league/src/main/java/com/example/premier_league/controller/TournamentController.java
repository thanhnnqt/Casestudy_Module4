package com.example.premier_league.controller;

import com.example.premier_league.entity.Team;
import com.example.premier_league.entity.Tournament;
import com.example.premier_league.service.ITeamService;
import com.example.premier_league.service.ITournamentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class TournamentController {
    private final ITournamentService tournamentService;
    private final ITeamService teamService;

    public TournamentController(ITournamentService tournamentService, ITeamService teamService) {
        this.tournamentService = tournamentService;
        this.teamService = teamService;
    }


    @GetMapping("/tournaments")
    public String list(Model model) {
        model.addAttribute("tournaments", tournamentService.findAll());
        return "tournament/list";
    }


    @GetMapping("/tournaments/add")
    public String addForm(Model model) {
        model.addAttribute("tournament", new Tournament());
        return "tournament/add";
    }


    @PostMapping("/tournaments/add")
    public String save(@ModelAttribute Tournament tournament) {
        tournamentService.save(tournament);
        return "redirect:/tournaments";
    }


    @GetMapping("/tournaments/delete/{id}")
    public String delete(@PathVariable Long id) {
        tournamentService.delete(id);
        return "redirect:/tournaments";
    }

    @GetMapping("/tournaments/{id}/manage-teams")
    public String showManageTeamsForm(@PathVariable Long id, Model model) {
        Tournament tournament = tournamentService.findById(id);
        if (tournament == null) {
            return "redirect:/tournaments";
        }

        // Lấy danh sách đội "có sẵn"
        List<Team> allTeams = teamService.findAll();

        // Lấy danh sách ID các đội đã có trong giải đấu
        model.addAttribute("tournament", tournament);
        model.addAttribute("allTeams", allTeams);
        model.addAttribute("tournamentTeamIds",
                tournament.getTeams().stream()
                        .map(Team::getId)
                        .collect(Collectors.toSet())
        );

        return "tournament/manage-teams";
    }

    // --- (PHẦN MỚI) LƯU CÁC ĐỘI ĐÃ CHỌN ---
    @PostMapping("/tournaments/save-teams")
    public String saveTeamsForTournament(@RequestParam("tournamentId") Long tournamentId,
                                         @RequestParam(value = "teamIds", required = false) List<Long> teamIds,
                                         RedirectAttributes redirect) {

        tournamentService.updateTeamsForTournament(tournamentId, teamIds);

        redirect.addFlashAttribute("message", "Cập nhật danh sách đội thành công!");
        // Quay lại đúng trang vừa sửa
        return "redirect:/tournaments/" + tournamentId + "/manage-teams";
    }
}
