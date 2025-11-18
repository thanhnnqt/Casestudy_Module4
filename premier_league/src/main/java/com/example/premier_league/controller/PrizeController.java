package com.example.premier_league.controller;

import com.example.premier_league.dto.PrizeDto;
import com.example.premier_league.entity.Player;
import com.example.premier_league.entity.Prize;
import com.example.premier_league.entity.Team;
import com.example.premier_league.service.IPlayerService;
import com.example.premier_league.service.IPrizeService;
import com.example.premier_league.service.ITeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequestMapping("/prize")
@RequiredArgsConstructor
public class PrizeController {

    private final IPrizeService prizeService;
    private final ITeamService teamService;
    private final IPlayerService playerService;

    @GetMapping
    public String showList(Model model) {
        model.addAttribute("prizes", prizeService.findAll());
        return "prize/prize-list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("prizeDto", new PrizeDto());
        model.addAttribute("teams", teamService.findAll());
        model.addAttribute("players", playerService.findAll());
        return "prize/prize-form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("prizeDto") PrizeDto prizeDto,
                       BindingResult bindingResult,
                       RedirectAttributes redirect,
                       Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("teams", teamService.findAll());
            model.addAttribute("players", playerService.findAll());
            return "prize/prize-form";
        }

        Prize prize;
        if (prizeDto.getId() != null) {
            prize = prizeService.findById(prizeDto.getId());
        } else {
            prize = new Prize();
        }

        BeanUtils.copyProperties(prizeDto, prize, "teamId", "playerId");

        prize.setTeam(null);
        prize.setPlayer(null);

        if (prizeDto.getPlayerId() != null) {
            Player player = playerService.findById(prizeDto.getPlayerId());
            if (player != null) {
                prize.setPlayer(player);
                prize.setTeam(player.getTeam());
            }
        } else if (prizeDto.getTeamId() != null) {
            Team team = teamService.findById(prizeDto.getTeamId());
            prize.setTeam(team);
        }

        prizeService.save(prize);
        redirect.addFlashAttribute("message", "Lưu giải thưởng thành công!");
        return "redirect:/prize";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Prize prize = prizeService.findById(id);
        if (prize == null) {
            return "redirect:/prize";
        }

        // Chuyển Entity sang DTO
        PrizeDto prizeDto = new PrizeDto();
        BeanUtils.copyProperties(prize, prizeDto);
        if (prize.getTeam() != null) {
            prizeDto.setTeamId(prize.getTeam().getId());
        }
        if (prize.getPlayer() != null) {
            prizeDto.setPlayerId(prize.getPlayer().getId());
        }

        model.addAttribute("prizeDto", prizeDto);
        model.addAttribute("teams", teamService.findAll());
        model.addAttribute("players", playerService.findAll());
        return "prize/prize-form";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        prizeService.delete(id);
        redirect.addFlashAttribute("message", "Đã xóa giải thưởng!");
        return "redirect:/prize";
    }
}