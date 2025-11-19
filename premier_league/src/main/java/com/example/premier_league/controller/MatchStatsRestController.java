package com.example.premier_league.controller;

import com.example.premier_league.dto.MatchStatsDto;
import com.example.premier_league.entity.MatchStats;
import com.example.premier_league.service.IMatchStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/matches")
public class MatchStatsRestController {

    private final IMatchStatsService statsService;
    private final SimpMessagingTemplate ws;

    @PostMapping("/{matchId}/stats")
    public ResponseEntity<?> updateStats(
            @PathVariable Long matchId,
            @RequestBody MatchStatsDto dto
    ) {
        MatchStats saved = statsService.updateStats(matchId, dto);

        // bắn realtime ra FE
        ws.convertAndSend("/topic/match/" + matchId + "/stats", saved);

        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{matchId}/stats")
    public ResponseEntity<?> getStats(@PathVariable Long matchId) {
        return ResponseEntity.ok(statsService.getStats(matchId));
    }
}
