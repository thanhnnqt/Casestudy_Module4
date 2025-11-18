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
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final IRankingService rankingService;
    private final IMatchEventRepository eventRepository;


    @Override
    @Transactional
    public MatchEvent createEvent(MatchEvent event) {
        // load match
        Match match = matchRepo.findById(event.getMatchId()).orElseThrow(() -> new RuntimeException("Match not found"));

        // block events if match is POSTPONED or FINISHED (except maybe administrative flow)
        if (match.getStatus() == MatchStatus.POSTPONED) {
            throw new RuntimeException("Match is postponed. Cannot add events.");
        }
        if (match.getStatus() == MatchStatus.FINISHED) {
            throw new RuntimeException("Match already finished. Cannot add events.");
        }

        // persist event (append-only)
        MatchEvent saved = eventRepo.save(event);

        // handle types
        String type = event.getType() == null ? "" : event.getType().trim().toUpperCase();

        if ("GOAL".equals(type)) {
            // only update score if match is LIVE
            if (match.getStatus() != MatchStatus.LIVE) {
                throw new RuntimeException("Match is not LIVE. Cannot add GOAL.");
            }

            if (event.getTeamId() != null) {
                Long teamId = event.getTeamId();
                if (teamId.equals(match.getHomeTeam().getId())) {
                    match.setHomeScore((match.getHomeScore() == null ? 0 : match.getHomeScore()) + 1);
                } else if (teamId.equals(match.getAwayTeam().getId())) {
                    match.setAwayScore((match.getAwayScore() == null ? 0 : match.getAwayScore()) + 1);
                }
            }
            matchRepo.save(match);

            // broadcast
            simpMessagingTemplate.convertAndSend("/topic/events", saved);
            simpMessagingTemplate.convertAndSend("/topic/match/" + match.getId() + "/score", match);
            return saved;
        }

        if ("MATCH_END".equals(type)) {
            // only allowed if match is LIVE
            if (match.getStatus() != MatchStatus.LIVE) {
                throw new RuntimeException("Match is not LIVE. Cannot end match.");
            }

            match.setStatus(MatchStatus.FINISHED);
            matchRepo.save(match);

            // apply ranking changes (points and stats)
            rankingService.applyMatchResult(match);

            // broadcast event, final score and updated ranking
            simpMessagingTemplate.convertAndSend("/topic/events", saved);
            simpMessagingTemplate.convertAndSend("/topic/match/" + match.getId() + "/score", match);
            simpMessagingTemplate.convertAndSend("/topic/rankings", rankingService.getRanking());

            return saved;
        }

        // default: other events (YELLOW_CARD, RED_CARD, PENALTY, ...)
        // If event indicates match start (optional) -> change status to LIVE (we won't auto-change for UPCOMING per requirement)
        // We simply broadcast the event.
        simpMessagingTemplate.convertAndSend("/topic/events", saved);
        return saved;
    }

    @Override
    public List<MatchEvent> listEvents(Long matchId) {
        return eventRepo.findByMatchIdOrderByCreatedAtAsc(matchId);
    }

    @Override
    public Match findMatchById(Long matchId) {
        return matchRepo.findById(matchId).orElse(null);
    }

    @Override
    public MatchEvent getEvent(Long id) {
        return eventRepo.findById(id).orElse(null);
    }

    @Override
    public List<MatchEvent> getEventsByMatch(Long matchId) {
        return eventRepository.findByMatchIdOrderByMinuteAsc(matchId);
    }
}
