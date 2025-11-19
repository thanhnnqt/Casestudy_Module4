package com.example.premier_league.controller;

import com.example.premier_league.entity.MatchEvent;
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

    /** Tạo sự kiện mới */
    @PostMapping("/matches/{matchId}/events")
    public ResponseEntity<?> createEvent(@PathVariable Long matchId, @RequestBody MatchEvent event) {
        try {
            event.setMatchId(matchId);
            MatchEvent saved = eventService.createEvent(event);
            return ResponseEntity.ok(saved);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }


    /** Lấy tất cả event của trận */
    @GetMapping("/{matchId}/events")
    public ResponseEntity<List<MatchEvent>> list(@PathVariable Long matchId) {
        return ResponseEntity.ok(eventService.getEventsByMatch(matchId));
    }

    /** Lấy thông tin trận đấu + tỉ số */
    @GetMapping("/{matchId}/score")
    public ResponseEntity<?> score(@PathVariable Long matchId) {
        var m = eventService.findMatchById(matchId);
        if (m == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(m);
    }
}
