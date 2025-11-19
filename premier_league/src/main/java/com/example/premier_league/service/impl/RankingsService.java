package com.example.premier_league.service.impl;

import com.example.premier_league.dto.RankingsDto;
import com.example.premier_league.entity.Match;
import com.example.premier_league.entity.Team;
import com.example.premier_league.repository.IMatchRepository;
import com.example.premier_league.repository.ITeamRepository;
import com.example.premier_league.service.IRankingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RankingsService implements IRankingsService {

    private final ITeamRepository teamRepository;
    private final IMatchRepository matchRepository; // thêm repo match

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

//    @Override
//    public List<RankingsDto> getRanking() {
//        List<Team> teams = teamRepository.findAll();
//        List<Match> matches = matchRepository.findAll(); // tất cả trận đã diễn ra
//
//        // reset tất cả thống kê
//        teams.forEach(t -> {
//            t.setPoints(0);
//            t.setWinCount(0);
//            t.setDrawCount(0);
//            t.setLoseCount(0);
//            t.setGoalsFor(0);
//            t.setGoalsAgainst(0);
//            t.setGoalDifference(0);
//        });
//
//        // tính lại dựa trên kết quả trận đấu
//        for (Match m : matches) {
//            if (m.getHomeScore() == null || m.getAwayScore() == null) continue;
//
//            Team home = teams.stream().filter(t -> t.getId().equals(m.getHomeTeam().getId())).findFirst().orElse(null);
//            Team away = teams.stream().filter(t -> t.getId().equals(m.getAwayTeam().getId())).findFirst().orElse(null);
//            if (home == null || away == null) continue;
//
//            int homeGoals = m.getHomeScore();
//            int awayGoals = m.getAwayScore();
//
//            home.setGoalsFor(home.getGoalsFor() + homeGoals);
//            home.setGoalsAgainst(home.getGoalsAgainst() + awayGoals);
//            away.setGoalsFor(away.getGoalsFor() + awayGoals);
//            away.setGoalsAgainst(away.getGoalsAgainst() + homeGoals);
//
//            home.setGoalDifference(home.getGoalsFor() - home.getGoalsAgainst());
//            away.setGoalDifference(away.getGoalsFor() - away.getGoalsAgainst());
//
//            if (homeGoals > awayGoals) {
//                home.setWinCount(home.getWinCount() + 1);
//                home.setPoints(home.getPoints() + 3);
//                away.setLoseCount(away.getLoseCount() + 1);
//            } else if (homeGoals < awayGoals) {
//                away.setWinCount(away.getWinCount() + 1);
//                away.setPoints(away.getPoints() + 3);
//                home.setLoseCount(home.getLoseCount() + 1);
//            } else {
//                home.setDrawCount(home.getDrawCount() + 1);
//                away.setDrawCount(away.getDrawCount() + 1);
//                home.setPoints(home.getPoints() + 1);
//                away.setPoints(away.getPoints() + 1);
//            }
//        }
//
//        // chuyển sang DTO và sắp xếp
//        return teams.stream()
//                .map(t -> {
//                    RankingsDto dto = new RankingsDto();
//                    dto.id = t.getId();
//                    dto.name = t.getName();
//                    dto.logoUrl = t.getLogoUrl();
//                    dto.points = t.getPoints();
//                    dto.winCount = t.getWinCount();
//                    dto.drawCount = t.getDrawCount();
//                    dto.loseCount = t.getLoseCount();
//                    dto.goalsFor = t.getGoalsFor();
//                    dto.goalsAgainst = t.getGoalsAgainst();
//                    dto.goalDifference = t.getGoalDifference();
//                    return dto;
//                })
//                .sorted(Comparator
//                        .comparingInt((RankingsDto r) -> r.points).reversed()
//                        .thenComparingInt(r -> r.goalDifference).reversed()
//                        .thenComparingInt(r -> r.goalsFor).reversed()
//                        .thenComparing(r -> r.name))
//                .toList();
//    }

    public List<RankingsDto> getRanking() {
        List<Team> teams = teamRepository.findAll();

        return teams.stream()
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
