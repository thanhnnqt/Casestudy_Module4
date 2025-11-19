package com.example.premier_league.controller;

import com.example.premier_league.dto.RankingsDto;
import com.example.premier_league.entity.Team;
import com.example.premier_league.service.IRankingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rankings/v1")
@RequiredArgsConstructor
public class RankingsRestController {

    private final IRankingsService rankingsService;

    @GetMapping
    public List<RankingsDto> getRanking() {
        return rankingsService.getRanking();
    }

    @GetMapping("/{id}")
    public Team getTeam(@PathVariable Long id) {
        return rankingsService.findById(id);
    }
}

