package com.example.premier_league.controller;

import com.example.premier_league.dto.MatchDto;
import com.example.premier_league.entity.Match;
import com.example.premier_league.repository.IMatchRepository;
import com.example.premier_league.service.impl.MatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class MatchRestController {

    private final MatchService matchService;
    private final IMatchRepository matchRepository;

    /* ===========================
     * Lấy tất cả trận đấu
     * =========================== */
    @GetMapping
    public List<Match> getAll() {
        return matchRepository.findAll();
    }

    /* ===========================
     * Lấy trận đấu theo ID
     * FE cần API này để load home/away team
     * =========================== */
    @GetMapping("/{id}")
    public MatchDto getById(@PathVariable Long id) {
        Match match = matchService.findById(id);
        if (match == null) return null;

        MatchDto dto = new MatchDto();

        dto.id = match.getId();
        dto.homeTeamId = match.getHomeTeam().getId();
        dto.awayTeamId = match.getAwayTeam().getId();
        dto.homeTeamName = match.getHomeTeam().getName();
        dto.awayTeamName = match.getAwayTeam().getName();
        dto.homeScore = match.getHomeScore();
        dto.awayScore = match.getAwayScore();
        dto.status = match.getStatus().name();
        dto.stadium = match.getStadium();

        if (match.getMatchDate() != null) {
            dto.matchDate = match.getMatchDate().toLocalDate().toString();   // yyyy-MM-dd
            dto.matchTime = match.getMatchDate().toLocalTime().toString().substring(0,5);
        }

        return dto;
    }


    /* ===========================
     * Tạo mới trận đấu
     * =========================== */
    @PostMapping
    public Match create(@RequestBody Match match) {
        return matchService.createMatch(match);
    }

    /* ===========================
     * Cập nhật trận đấu
     * =========================== */
    @PutMapping("/{id}")
    public Match update(@PathVariable Long id, @RequestBody Match match) {
        return matchService.updateMatch(id, match);
    }

    /* ===========================
     * Xóa trận đấu
     * =========================== */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        matchRepository.deleteById(id);
    }
}
