package com.example.premier_league.controller;

import com.example.premier_league.dto.MatchEventDto;
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
    @PostMapping("/{matchId}/events")
    public ResponseEntity<?> addEvent(
            @PathVariable Long matchId,
            @RequestBody MatchEventDto dto
    ) {
        eventService.addEvent(matchId, dto);
        return ResponseEntity.ok("success");
    }

    /** Lấy tất cả event của trận */
    @GetMapping("/{matchId}/events")
    public ResponseEntity<List<MatchEvent>> list(@PathVariable Long matchId) {
        return ResponseEntity.ok(eventService.getEventsByMatch(matchId));
    }

    /** Lấy thông tin tỉ số theo trận */
    @GetMapping("/{matchId}/score")
    public ResponseEntity<?> score(@PathVariable Long matchId) {
        var m = eventService.findMatchById(matchId);
        if (m == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(m);
    }
}

