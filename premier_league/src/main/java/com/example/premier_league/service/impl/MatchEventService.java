package com.example.premier_league.service.impl;

import com.example.premier_league.dto.MatchEventDto;
import com.example.premier_league.dto.MatchEventResponse;
import com.example.premier_league.entity.*;
import com.example.premier_league.repository.IMatchEventRepository;
import com.example.premier_league.repository.IMatchRepository;
import com.example.premier_league.repository.IPlayerRepository;
import com.example.premier_league.repository.ITeamRepository;
import com.example.premier_league.service.IMatchEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MatchEventService implements IMatchEventService {

    private final IMatchEventRepository eventRepo;
    private final IMatchRepository matchRepo;
    private final SimpMessagingTemplate messaging;
    private final ITeamRepository teamRepo;
    private final IPlayerRepository playerRepo;
    private final RankingsService rankingsService;

    private MatchEventResponse toDto(MatchEvent e) {
        return new MatchEventResponse(
                e.getId(),
                e.getMinute(),
                e.getType(),
                e.getDescription(),
                e.getTeam() != null ? e.getTeam().getName() : null,
                e.getPlayer() != null ? e.getPlayer().getName() : null
        );
    }

    @Override
    @Transactional
    public MatchEvent createEvent(MatchEvent event) {

        Long matchId = event.getMatch().getId();
        Match match = matchRepo.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy trận"));

        // resolve team
        if (event.getTeam() != null && event.getTeam().getId() != null) {
            Team t = teamRepo.findById(event.getTeam().getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy team"));
            event.setTeam(t);
        }

        // resolve player
        if (event.getPlayer() != null && event.getPlayer().getId() != null) {
            Player p = playerRepo.findById(event.getPlayer().getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy cầu thủ"));
            event.setPlayer(p);
        }

        event.setMatch(match);
        MatchEvent saved = eventRepo.save(event);

        // ============================ GOAL =============================
        if ("GOAL".equalsIgnoreCase(saved.getType())) {

            if (match.getStatus() != MatchStatus.LIVE) {
                match.setStatus(MatchStatus.LIVE);
                matchRepo.save(match);
            }


            Long tid = saved.getTeam().getId();
            if (tid.equals(match.getHomeTeam().getId()))
                match.setHomeScore(match.getHomeScore() + 1);
            else
                match.setAwayScore(match.getAwayScore() + 1);

            matchRepo.save(match);

            Map<String, Object> score = new HashMap<>();
            score.put("homeScore", match.getHomeScore());
            score.put("awayScore", match.getAwayScore());
            score.put("status", match.getStatus().name());

            messaging.convertAndSend("/topic/match/" + matchId + "/score", score);
        }

        // ============================ MATCH END =============================
        if ("MATCH_END".equalsIgnoreCase(saved.getType())) {

            match.setStatus(MatchStatus.FINISHED);
            matchRepo.save(match);

            Map<String, Object> finish = new HashMap<>();
            finish.put("homeScore", match.getHomeScore());
            finish.put("awayScore", match.getAwayScore());
            finish.put("status", match.getStatus().name());
            messaging.convertAndSend("/topic/match/" + matchId + "/score", finish);

            messaging.convertAndSend("/topic/match/" + matchId + "/events", toDto(saved));

            // ⭐⭐⭐ cập nhật BXH duy nhất tại đây ⭐⭐⭐
            rankingsService.applyMatchResult(match);

            return saved;
        }

        // sự kiện thường
        messaging.convertAndSend("/topic/match/" + matchId + "/events", toDto(saved));
        return saved;
    }

    @Override
    public List<MatchEvent> listEvents(Long matchId) {
        return eventRepo.findByMatchIdOrderByMinuteAsc(matchId);
    }

    @Override
    public List<MatchEventResponse> getEventsByMatch(Long matchId) {
        return eventRepo.findByMatchIdOrderByMinuteAsc(matchId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void addEvent(Long matchId, MatchEventDto dto) {

        Match match = matchRepo.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy trận"));

        MatchEvent e = new MatchEvent();
        e.setMatch(match);

        if (match.getStatus() != MatchStatus.LIVE &&
                !"MATCH_END".equalsIgnoreCase(dto.getType())) {
            match.setStatus(MatchStatus.LIVE);
            matchRepo.save(match);
        }

        e.setMinute(dto.getMinute());
        e.setType(dto.getType());
        e.setDescription(dto.getDescription());

        if (dto.getTeamId() != null)
            e.setTeam(teamRepo.findById(dto.getTeamId()).orElseThrow());

        if (dto.getPlayerId() != null)
            e.setPlayer(playerRepo.findById(dto.getPlayerId()).orElseThrow());

        createEvent(e);
    }

    @Override
    public Match findMatchById(Long matchId) {
        return matchRepo.findById(matchId).orElse(null);
    }

    @Override
    public MatchEvent getEvent(Long id) {
        return eventRepo.findById(id).orElse(null);
    }
}
