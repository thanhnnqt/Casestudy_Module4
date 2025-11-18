package com.example.premier_league.service.impl;

import com.example.premier_league.dto.RankingDto;
import com.example.premier_league.entity.Match;
import com.example.premier_league.entity.Team;
import com.example.premier_league.repository.IMatchRepository;
import com.example.premier_league.repository.ITeamRepository;
import com.example.premier_league.service.IRankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RankingService implements IRankingService {

    private final ITeamRepository teamRepository;
    private final IMatchRepository matchRepository;

    @Override
    @Transactional
    public void applyMatchResult(Match match) {
        Team home = teamRepository.findById(match.getHomeTeam().getId())
                .orElseThrow(() -> new RuntimeException("Home team not found"));

        Team away = teamRepository.findById(match.getAwayTeam().getId())
                .orElseThrow(() -> new RuntimeException("Away team not found"));

        int homeGoals = match.getHomeScore() == null ? 0 : match.getHomeScore();
        int awayGoals = match.getAwayScore() == null ? 0 : match.getAwayScore();

        home.setGoalsFor(home.getGoalsFor() + homeGoals);
        home.setGoalsAgainst(home.getGoalsAgainst() + awayGoals);

        away.setGoalsFor(away.getGoalsFor() + awayGoals);
        away.setGoalsAgainst(away.getGoalsAgainst() + homeGoals);

        home.setGoalDifference(home.getGoalsFor() - home.getGoalsAgainst());
        away.setGoalDifference(away.getGoalsFor() - away.getGoalsAgainst());

        if (homeGoals > awayGoals) {
            home.setWinCount(home.getWinCount() + 1);
            away.setLoseCount(away.getLoseCount() + 1);
            home.setPoints(home.getPoints() + 3);
        } else if (homeGoals < awayGoals) {
            away.setWinCount(away.getWinCount() + 1);
            home.setLoseCount(home.getLoseCount() + 1);
            away.setPoints(away.getPoints() + 3);
        } else {
            home.setDrawCount(home.getDrawCount() + 1);
            away.setDrawCount(away.getDrawCount() + 1);
            home.setPoints(home.getPoints() + 1);
            away.setPoints(away.getPoints() + 1);
        }

        teamRepository.save(home);
        teamRepository.save(away);
    }

    @Override
    public List<RankingDto> getRanking() {
        return teamRepository.findAll()
                .stream()
                .map(t -> {
                    RankingDto dto = new RankingDto();
                    dto.teamName = t.getName();
                    dto.logoUrl = t.getLogoUrl();
                    dto.points = t.getPoints();
                    dto.goalsFor = t.getGoalsFor();
                    dto.goalsAgainst = t.getGoalsAgainst();
                    dto.goalDifference = t.getGoalDifference();
                    return dto;
                })
                .sorted(Comparator
                        .comparingInt((RankingDto r) -> r.points).reversed()
                        .thenComparingInt(r -> r.goalDifference).reversed()
                        .thenComparingInt(r -> r.goalsFor).reversed()
                        .thenComparing(r -> r.teamName))
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
