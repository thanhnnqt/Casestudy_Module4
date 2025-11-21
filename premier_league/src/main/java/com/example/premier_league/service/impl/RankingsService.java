package com.example.premier_league.service.impl;

import com.example.premier_league.dto.RankingsDto;
import com.example.premier_league.entity.Match;
import com.example.premier_league.entity.Team;
import com.example.premier_league.repository.ITeamRepository;
import com.example.premier_league.service.IRankingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RankingsService implements IRankingsService {

    private final ITeamRepository teamRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public void applyMatchResult(Match match) {

        Team home = teamRepository.findById(match.getHomeTeam().getId()).orElse(null);
        Team away = teamRepository.findById(match.getAwayTeam().getId()).orElse(null);
        if (home == null || away == null) return;

        int hs = match.getHomeScore() == null ? 0 : match.getHomeScore();
        int as = match.getAwayScore() == null ? 0 : match.getAwayScore();

        // update goals
        home.setGoalsFor(home.getGoalsFor() + hs);
        home.setGoalsAgainst(home.getGoalsAgainst() + as);
        away.setGoalsFor(away.getGoalsFor() + as);
        away.setGoalsAgainst(away.getGoalsAgainst() + hs);

        // update W/D/L + points
        if (hs > as) {
            home.setWinCount(home.getWinCount() + 1);
            home.setPoints(home.getPoints() + 3);
            away.setLoseCount(away.getLoseCount() + 1);
        } else if (hs < as) {
            away.setWinCount(away.getWinCount() + 1);
            away.setPoints(away.getPoints() + 3);
            home.setLoseCount(home.getLoseCount() + 1);
        } else {
            home.setDrawCount(home.getDrawCount() + 1);
            away.setDrawCount(away.getDrawCount() + 1);
            home.setPoints(home.getPoints() + 1);
            away.setPoints(away.getPoints() + 1);
        }

        // update GD
        home.setGoalDifference(home.getGoalsFor() - home.getGoalsAgainst());
        away.setGoalDifference(away.getGoalsFor() - away.getGoalsAgainst());

        teamRepository.save(home);
        teamRepository.save(away);

        // websocket broadcast
        List<RankingsDto> updated = getRanking();
        messagingTemplate.convertAndSend("/topic/ranking-updated", updated);
    }

    @Override
    public List<RankingsDto> getRanking() {
        return teamRepository.findAll().stream()
                .map(t -> new RankingsDto(
                        t.getId(),
                        t.getName(),
                        t.getLogoUrl(),
                        t.getPoints(),
                        t.getWinCount(),
                        t.getDrawCount(),
                        t.getLoseCount(),
                        t.getGoalsFor(),
                        t.getGoalsAgainst(),
                        t.getGoalDifference()
                ))
                .sorted(Comparator
                        .comparingInt((RankingsDto t) -> t.points).reversed()
                        .thenComparingInt(t -> t.goalDifference).reversed()
                        .thenComparingInt(t -> t.goalsFor).reversed()
                )
                .toList();
    }

    @Override
    public Team findById(Long id) {
        return teamRepository.findById(id).orElse(null);
    }

    @Override
    public void save(Team teamUpdate) {
        teamRepository.save(teamUpdate);
    }

    @Override
    public void delete(Long id) {
        teamRepository.deleteById(id);
    }
}
