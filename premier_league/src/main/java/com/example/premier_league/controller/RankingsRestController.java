package com.example.premier_league.controller;

import com.example.premier_league.dto.RankingsDto;
import com.example.premier_league.entity.Team;
import com.example.premier_league.service.IRankingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rankings/v1")
@RequiredArgsConstructor
public class RankingsRestController {

    private final IRankingsService rankingsService;
    private final SimpMessagingTemplate messagingTemplate;


    @GetMapping
    public List<RankingsDto> getRanking() {
        return rankingsService.getRanking();
    }

    @GetMapping("/{id}")
    public Team getTeam(@PathVariable Long id) {
        return rankingsService.findById(id);
    }

    @PatchMapping("/{id}")
    public void updateRank(@PathVariable Long id, @RequestBody Team team) {
        Team t = rankingsService.findById(id);
        BeanUtils.copyProperties(team, t);
        rankingsService.save(t);

        // thông báo FE reload bảng
        messagingTemplate.convertAndSend("/topic/ranking-updated", "updated");
    }

    @DeleteMapping("/{id}")
    public void deleteRankings(@PathVariable Long id) {
        rankingsService.delete(id);
    }
}

