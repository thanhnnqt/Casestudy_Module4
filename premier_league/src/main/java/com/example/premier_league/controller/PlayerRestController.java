package com.example.premier_league.controller;

import com.example.premier_league.dto.PlayerShortDto;
import com.example.premier_league.service.IPlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class PlayerRestController {

    private final IPlayerService playerService;

    @GetMapping("/{teamId}/players")
    public List<PlayerShortDto> getPlayers(@PathVariable Long teamId) {
        return playerService.getPlayersByTeam(teamId);
    }
}

