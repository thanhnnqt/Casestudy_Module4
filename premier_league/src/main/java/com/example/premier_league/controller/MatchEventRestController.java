package com.example.premier_league.controller;

import com.example.premier_league.entity.Match;
import com.example.premier_league.entity.MatchEvent;
import com.example.premier_league.service.IMatchEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class MatchEventRestController {

    private final IMatchEventService eventService;

    // Tạo sự kiện mới
    @PostMapping("/{matchId}/events")
    public ResponseEntity<?> createEvent(
            @PathVariable Long matchId,
            @RequestBody MatchEvent event
    ) {
        try {
            event.setMatchId(matchId);
            MatchEvent saved = eventService.createEvent(event);
            return ResponseEntity.ok(saved);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    // Lấy tất cả sự kiện ban đầu
    @GetMapping("/{matchId}/events")
    public ResponseEntity<List<MatchEvent>> getEvents(@PathVariable Long matchId) {
        return ResponseEntity.ok(eventService.getEventsByMatch(matchId));
    }

    // Lấy thông tin trận
    @GetMapping("/{matchId}/score")
    public ResponseEntity<?> getScore(@PathVariable Long matchId) {
        Match m = eventService.findMatchById(matchId);
        if (m == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(m);
    }
}
