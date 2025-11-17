package com.example.premier_league.controller;

import com.example.premier_league.dto.TeamDto;
import com.example.premier_league.entity.Team;
import com.example.premier_league.exception.TeamNotFoundException;
import com.example.premier_league.service.ITeamService;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequestMapping("/teams")
public class TeamController {

    private final ITeamService teamService;

    public TeamController(ITeamService teamService) {
        this.teamService = teamService;
    }

    // ==================== HIỂN THỊ DANH SÁCH ====================
    @GetMapping
    public String listTeams(Model model) {
        model.addAttribute("teams", teamService.findAll());
        return "team/list";
    }

    // ==================== TẠO MỚI ====================
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("teamDto", new TeamDto());
        return "team/create";
    }

    @PostMapping("/create")
    public String createTeam(@Valid @ModelAttribute("teamDto") TeamDto teamDto,
                             BindingResult bindingResult,
                             RedirectAttributes redirect) {

        if (bindingResult.hasErrors()) {
            return "team/create";
        }

        Team team = new Team();
        BeanUtils.copyProperties(teamDto, team);
        teamService.save(team);

        redirect.addFlashAttribute("message", "Thêm mới đội bóng thành công!");
        return "redirect:/team";
    }

    // ==================== CHI TIẾT ====================
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Team team = teamService.findById(id);
        if (team == null) {
            throw new TeamNotFoundException("Không tìm thấy đội bóng với ID: " + id);
        }

        model.addAttribute("team", team);
        return "team/detail";
    }

    // ==================== CẬP NHẬT ====================
    @GetMapping("/update/{id}")
    public String updateForm(@PathVariable Long id, Model model) {
        Team team = teamService.findById(id);
        if (team == null) {
            throw new TeamNotFoundException("Không tìm thấy đội bóng với ID: " + id);
        }

        TeamDto teamDto = new TeamDto();
        BeanUtils.copyProperties(team, teamDto);

        model.addAttribute("teamDto", teamDto);
        return "team/update";
    }

    @PostMapping("/update")
    public String updateTeam(@Valid @ModelAttribute("teamDto") TeamDto teamDto,
                             BindingResult bindingResult,
                             RedirectAttributes redirect,
                             Model model) {

        if (bindingResult.hasErrors()) {
            return "team/update";
        }

        Team existingTeam = teamService.findById(teamDto.getId());
        if (existingTeam == null) {
            model.addAttribute("message", "Không tìm thấy đội bóng với ID: " + teamDto.getId());
            return "team/error";
        }

        BeanUtils.copyProperties(teamDto, existingTeam);
        teamService.update(existingTeam);

        redirect.addFlashAttribute("message", "Cập nhật đội bóng thành công!");
        return "redirect:/team";
    }

    // ==================== XÓA ====================
    @GetMapping("/delete/{id}")
    public String deleteTeam(@PathVariable Long id, RedirectAttributes redirect) {
        Team team = teamService.findById(id);
        if (team == null) {
            throw new TeamNotFoundException("Không tìm thấy đội bóng với ID: " + id);
        }

        teamService.delete(id);

        redirect.addFlashAttribute("message", "Xóa đội bóng thành công!");
        return "redirect:/team";
    }

//    // ==================== TÌM KIẾM ====================
//    @GetMapping("/search")
//    public String searchTeam(@RequestParam("name") String name, Model model) {
//        List<Team> teams = teamService.findByName(name);
//        model.addAttribute("teams", teams);
//        model.addAttribute("search", name);
//        return "team/list";
//    }
}
