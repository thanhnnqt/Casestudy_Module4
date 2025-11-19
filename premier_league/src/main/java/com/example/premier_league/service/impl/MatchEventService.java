package com.example.premier_league.service.impl;

import com.example.premier_league.entity.Match;
import com.example.premier_league.entity.MatchEvent;
import com.example.premier_league.entity.MatchStatus;
import com.example.premier_league.repository.IMatchEventRepository;
import com.example.premier_league.repository.IMatchRepository;
import com.example.premier_league.service.IMatchEventService;
import com.example.premier_league.service.IRankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchEventService implements IMatchEventService {

    private final IMatchEventRepository eventRepo;
    private final IMatchRepository matchRepo;
    private final SimpMessagingTemplate messaging;

    @Override
    @Transactional
    public MatchEvent createEvent(MatchEvent event) {

        var match = matchRepo.findById(event.getMatchId())
                .orElseThrow(() -> new RuntimeException("Match not found"));

        // Lưu event
        MatchEvent saved = eventRepo.save(event);

        // Nếu là bàn thắng → cập nhật tỉ số
        if ("GOAL".equalsIgnoreCase(event.getType())) {

            if (match.getStatus() != MatchStatus.LIVE) {
                throw new RuntimeException("Match is not LIVE. Cannot add goal.");
            }

            if (event.getTeamId().equals(match.getHomeTeam().getId())) {
                match.setHomeScore(match.getHomeScore() + 1);
            } else if (event.getTeamId().equals(match.getAwayTeam().getId())) {
                match.setAwayScore(match.getAwayScore() + 1);
            }

            matchRepo.save(match);

            // Gửi realtime
            messaging.convertAndSend("/topic/match/" + match.getId() + "/score", match);
        }

        // Gửi realtime sự kiện mới thêm
        messaging.convertAndSend("/topic/match/" + match.getId() + "/events", saved);

        return saved;
    }

    @Override
    public List<MatchEvent> listEvents(Long matchId) {
        return eventRepo.findByMatchIdOrderByMinuteAsc(matchId);
    }

    @Override
    public List<MatchEvent> getEventsByMatch(Long matchId) {
        return eventRepo.findByMatchIdOrderByMinuteAsc(matchId);
    }

    @Override
    public Match findMatchById(Long matchId) {
        return matchRepo.findById(matchId).orElse(null);
    }

    @Override
    public MatchEvent getEvent(Long id) {
        return null;
    }
}

