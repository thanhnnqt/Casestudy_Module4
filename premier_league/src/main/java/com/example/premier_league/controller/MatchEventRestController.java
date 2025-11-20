package com.example.premier_league.controller;

import com.example.premier_league.dto.MatchDto;
import com.example.premier_league.dto.MatchEventDto;
import com.example.premier_league.dto.MatchEventResponse;
import com.example.premier_league.service.IMatchEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/matches")
public class MatchEventRestController {

    private final IMatchEventService eventService;

    @PostMapping("/{matchId}/events")
    public ResponseEntity<?> addEvent(
            @PathVariable Long matchId,
            @RequestBody MatchEventDto dto
    ) {
        eventService.addEvent(matchId, dto);
        return ResponseEntity.ok("success");
    }

    @GetMapping("/{matchId}/events")
    public ResponseEntity<List<MatchEventResponse>> list(@PathVariable Long matchId) {
        return ResponseEntity.ok(eventService.getEventsByMatch(matchId));
    }

    @GetMapping("/{matchId}/score")
    public ResponseEntity<?> score(@PathVariable Long matchId) {
        var m = eventService.findMatchById(matchId);
        if (m == null) return ResponseEntity.notFound().build();

        MatchDto dto = new MatchDto();
        dto.id = m.getId();
        dto.homeTeamId = m.getHomeTeam().getId();
        dto.awayTeamId = m.getAwayTeam().getId();
        dto.homeTeamName = m.getHomeTeam().getName();
        dto.awayTeamName = m.getAwayTeam().getName();
        dto.homeScore = m.getHomeScore();
        dto.awayScore = m.getAwayScore();
        dto.status = m.getStatus().name();
        dto.stadium = m.getHomeTeam().getStadium();   // ⭐ LẤY SÂN NHÀ
        dto.matchDate = m.getMatchDate().toString();
        dto.homeLogo = m.getHomeTeam().getLogoUrl();
        dto.awayLogo = m.getAwayTeam().getLogoUrl();


        return ResponseEntity.ok(dto);
    }

}
