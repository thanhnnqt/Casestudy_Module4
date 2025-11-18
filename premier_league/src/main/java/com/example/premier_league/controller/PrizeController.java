package com.example.premier_league.controller;

import com.example.premier_league.entity.Prize;
import com.example.premier_league.service.IPlayerService;
import com.example.premier_league.service.IPrizeService;
import com.example.premier_league.service.ITeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


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
        model.addAttribute("prize", new Prize());
        model.addAttribute("teams", teamService.findAll());
        return "prize/prize-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Prize prize) {
        prizeService.save(prize);
        return "redirect:/prize";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("prize", prizeService.findById(id));
        model.addAttribute("teams", teamService.findAll());
        return "prize/prize-form";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        prizeService.delete(id);
        return "redirect:/prize";
    }
}
