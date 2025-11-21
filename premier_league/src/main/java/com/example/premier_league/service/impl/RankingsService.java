package com.example.premier_league.service.impl;

import com.example.premier_league.dto.RankingsDto;
import com.example.premier_league.entity.Match;
import com.example.premier_league.entity.Team;
import com.example.premier_league.repository.ITeamRepository;
import com.example.premier_league.repository.IMatchRepository;
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
    private final IMatchRepository matchRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Áp kết quả trận vào BXH. Idempotent: nếu trận đã được áp (rankingProcessed == true) thì không làm gì.
     */
    @Override
    @Transactional
    public void applyMatchResult(Match match) {
        if (match == null) return;

        // reload match to be safe (managed)
        Match managedMatch = matchRepository.findById(match.getId()).orElse(null);
        if (managedMatch == null) return;

        // nếu đã xử lý trước đó thì skip
        if (managedMatch.isRankingProcessed()) {
            // optional: log
            System.out.println("RankingsService: match id=" + managedMatch.getId() + " already processed for ranking. Skip.");
            return;
        }

        Team home = teamRepository.findById(managedMatch.getHomeTeam().getId()).orElse(null);
        Team away = teamRepository.findById(managedMatch.getAwayTeam().getId()).orElse(null);
        if (home == null || away == null) return;

        int hs = managedMatch.getHomeScore() == null ? 0 : managedMatch.getHomeScore();
        int as = managedMatch.getAwayScore() == null ? 0 : managedMatch.getAwayScore();

        // update goals
        home.setGoalsFor(home.getGoalsFor() + hs);
        home.setGoalsAgainst(home.getGoalsAgainst() + as);
        away.setGoalsFor(away.getGoalsFor() + as);
        away.setGoalsAgainst(away.getGoalsAgainst() + hs);

        // update win/draw/loss + points
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

        // save teams
        teamRepository.save(home);
        teamRepository.save(away);

        // mark match as processed and save
        managedMatch.setRankingProcessed(true);
        matchRepository.save(managedMatch);

        // broadcast BXH realtime
        List<RankingsDto> updated = getRanking();
        messagingTemplate.convertAndSend("/topic/ranking-updated", updated);

        System.out.println("RankingsService: applied match id=" + managedMatch.getId() + " to rankings.");
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
                        .comparingInt((RankingsDto r) -> r.points).reversed()
                        .thenComparingInt(r -> r.goalDifference).reversed()
                        .thenComparingInt(r -> r.goalsFor).reversed()
                        .thenComparing(r -> r.name))
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
