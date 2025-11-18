package com.example.premier_league.controller;

import com.example.premier_league.entity.Match;
import com.example.premier_league.entity.MatchEvent;
import com.example.premier_league.service.IMatchEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MatchEventRestController {

    private final IMatchEventService eventService;

    @PostMapping("/matches/{matchId}/events")
    public ResponseEntity<?> createEvent(@PathVariable Long matchId, @RequestBody MatchEvent event) {
        try {
            event.setMatchId(matchId);
            MatchEvent saved = eventService.createEvent(event);
            return ResponseEntity.ok(saved);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("Server error: " + ex.getMessage());
        }
    }

    @GetMapping("/matches/{matchId}/events")
    public ResponseEntity<List<MatchEvent>> getEvents(@PathVariable Long matchId) {
        return ResponseEntity.ok(eventService.listEvents(matchId));
    }

    @GetMapping("/matches/{matchId}/score")
    public ResponseEntity<?> getScore(@PathVariable Long matchId) {
        Match m = eventService.findMatchById(matchId);
        if (m == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(m);
    }
}
