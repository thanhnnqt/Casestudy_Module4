package com.example.premier_league.controller;

import com.example.premier_league.dto.PlayerDto;
import com.example.premier_league.entity.Player;
import com.example.premier_league.entity.Team;
import com.example.premier_league.exception.PlayerNotFoundException;
import com.example.premier_league.service.IPlayerService;
import com.example.premier_league.service.ITeamService;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/players")
public class PlayerController {

    private final IPlayerService playerService;
    private final ITeamService teamService;

    public PlayerController(IPlayerService playerService, ITeamService teamService) {
        this.playerService = playerService;
        this.teamService = teamService;
    }

    // ====== LIST ======
    @GetMapping
    public String list(Model model) {
        model.addAttribute("players", playerService.findAll());
        return "player/list";
    }

    // ====== CREATE ======
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("playerDto", new PlayerDto());
        model.addAttribute("teams", teamService.findAll());
        return "player/create";
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("playerDto") PlayerDto playerDto,
                         BindingResult bindingResult,
                         RedirectAttributes redirect,
                         Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("teams", teamService.findAll());
            return "player/create";
        }

        Player player = new Player();
        BeanUtils.copyProperties(playerDto, player);

        Team team = teamService.findById(playerDto.getTeamId());
        player.setTeam(team);

        playerService.save(player);

        redirect.addFlashAttribute("message", "Thêm cầu thủ thành công!");
        return "redirect:/admin/players";
    }

    // ====== DETAIL ======
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Player player = playerService.findById(id);
        if (player == null) {
            throw new PlayerNotFoundException("Không tìm thấy cầu thủ ID: " + id);
        }

        model.addAttribute("player", player);
        return "player/detail";
    }

    // ====== UPDATE ======
    @GetMapping("/update/{id}")
    public String showUpdateForm(@PathVariable Long id, Model model) {
        Player player = playerService.findById(id);
        if (player == null) {
            throw new PlayerNotFoundException("Không tìm thấy cầu thủ!");
        }

        PlayerDto dto = new PlayerDto();
        BeanUtils.copyProperties(player, dto);

        dto.setTeamId(player.getTeam().getId());

        model.addAttribute("playerDto", dto);
        model.addAttribute("teams", teamService.findAll());

        return "player/update";
    }

    @PostMapping("/update")
    public String update(@Valid @ModelAttribute("playerDto") PlayerDto playerDto,
                         BindingResult bindingResult,
                         RedirectAttributes redirect,
                         Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("teams", teamService.findAll());
            return "player/update";
        }

        Player existing = playerService.findById(playerDto.getId());
        if (existing == null) {
            throw new PlayerNotFoundException("Không tìm thấy cầu thủ!");
        }

        BeanUtils.copyProperties(playerDto, existing);
        existing.setTeam(teamService.findById(playerDto.getTeamId()));

        playerService.update(existing);

        redirect.addFlashAttribute("message", "Cập nhật cầu thủ thành công!");
        return "redirect:/admin/players";
    }

    // ====== DELETE ======
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        Player player = playerService.findById(id);
        if (player == null) {
            throw new PlayerNotFoundException("Không tìm thấy cầu thủ!");
        }

        playerService.delete(id);
        redirect.addFlashAttribute("message", "Xóa cầu thủ thành công!");
        return "redirect:/admin/players";
    }

    // ====== SEARCH ======
    @GetMapping("/search")
    public String search(@RequestParam("name") String name, Model model) {
        model.addAttribute("players", playerService.findByName(name));
        model.addAttribute("search", name);
        return "player/list";
    }
}
