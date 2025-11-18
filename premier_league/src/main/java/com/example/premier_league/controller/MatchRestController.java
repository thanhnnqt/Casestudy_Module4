package com.example.premier_league.controller;

import com.example.premier_league.entity.Match;
import com.example.premier_league.repository.IMatchRepository;
import com.example.premier_league.service.impl.MatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
class MatchRestController {


    private final MatchService matchService;
    private final IMatchRepository matchRepository;


    @GetMapping
    public List<Match> getAll() {
        return matchRepository.findAll();
    }


    @PostMapping
    public Match create(@RequestBody Match match) {
        return matchService.createMatch(match);
    }


    @PutMapping("/{id}")
    public Match update(@PathVariable Long id, @RequestBody Match match) {
        return matchService.updateMatch(id, match);
    }


    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        matchRepository.deleteById(id);
    }
}
