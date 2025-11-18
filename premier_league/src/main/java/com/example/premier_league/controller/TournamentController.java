package com.example.premier_league.controller;

import com.example.premier_league.entity.Tournament;
import com.example.premier_league.service.ITournamentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class TournamentController {
    private final ITournamentService tournamentService;

    public TournamentController(ITournamentService tournamentService) {
        this.tournamentService = tournamentService;
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


    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        tournamentService.delete(id);
        return "redirect:/tournaments";
    }
}
