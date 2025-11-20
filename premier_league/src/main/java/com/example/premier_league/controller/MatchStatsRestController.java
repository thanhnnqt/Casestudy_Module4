package com.example.premier_league.controller;

import com.example.premier_league.dto.MatchStatsDto;
import com.example.premier_league.entity.MatchStats;
import com.example.premier_league.service.IMatchStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class MatchStatsRestController {

    private final IMatchStatsService statsService;

    // ===== GET stats FE gọi để hiển thị =====
    @GetMapping("/{matchId}/stats")
    public MatchStatsDto getStats(@PathVariable Long matchId) {
        return statsService.getStatsByMatchId(matchId);
    }

    // ===== UPDATE stats Admin gửi =====
    @PutMapping("/{matchId}/stats")
    public ResponseEntity<?> updateStats(
            @PathVariable Long matchId,
            @RequestBody MatchStatsDto dto
    ) {
        statsService.updateStats(matchId, dto);
        return ResponseEntity.ok("updated");
    }
}

