package com.example.premier_league.serivce;

import com.example.premier_league.dto.MatchDto;
import com.example.premier_league.entity.Match;
import com.example.premier_league.entity.MatchStatus;
import com.example.premier_league.entity.Team;
import com.example.premier_league.repository.IMatchRepository;
import com.example.premier_league.repository.ITeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MatchService {


    private final IMatchRepository matchRepository;
    private final ITeamRepository teamRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final RankingService rankingService;


    public Match createMatch(Match m) {
        m.setStatus(MatchStatus.SCHEDULED);
        return matchRepository.save(m);
    }


    public Match updateMatch(Long id, Match updated) {
        Match old = matchRepository.findById(id).orElseThrow();


        old.setHomeScore(updated.getHomeScore());
        old.setAwayScore(updated.getAwayScore());
        old.setStadium(updated.getStadium());
        old.setMatchDate(updated.getMatchDate());
        old.setStatus(updated.getStatus());


        Match saved = matchRepository.save(old);


        if (saved.getStatus() == MatchStatus.FINISHED) {
            updateRankingAfterMatch(saved);
        }


        broadcastUpdates(saved);
        return saved;
    }


    private void updateRankingAfterMatch(Match match) {
        Team home = match.getHomeTeam();
        Team away = match.getAwayTeam();


        int hs = match.getHomeScore();
        int as = match.getAwayScore();


// Update GF, GA
        home.setGoalsFor(home.getGoalsFor() + hs);
        home.setGoalsAgainst(home.getGoalsAgainst() + as);
        away.setGoalsFor(away.getGoalsFor() + as);
        away.setGoalsAgainst(away.getGoalsAgainst() + hs);


// Update win/draw/loss
        if (hs > as) {
            home.setWinCount(home.getWinCount() + 1);
            away.setLoseCount(away.getLoseCount() + 1);
            home.setPoints(home.getPoints() + 3);
        } else if (hs < as) {
            away.setWinCount(away.getWinCount() + 1);
            home.setLoseCount(home.getLoseCount() + 1);
            away.setPoints(away.getPoints() + 3);
        } else {
            home.setDrawCount(home.getDrawCount() + 1);
            away.setDrawCount(away.getDrawCount() + 1);
            home.setPoints(home.getPoints() + 1);
            away.setPoints(away.getPoints() + 1);
        }


        home.setGoalDifference(home.getGoalsFor() - home.getGoalsAgainst());
        away.setGoalDifference(away.getGoalsFor() - away.getGoalsAgainst());


        teamRepository.save(home);
        teamRepository.save(away);
    }


    private void broadcastUpdates(Match match) {
        MatchDto dto = toDto(match);
        simpMessagingTemplate.convertAndSend("/topic/matches", dto);


        var ranking = rankingService.getRanking();
        simpMessagingTemplate.convertAndSend("/topic/rankings", ranking);
    }


    private MatchDto toDto(Match m) {
        MatchDto dto = new MatchDto();
        dto.id = m.getId();
        dto.homeTeamId = m.getHomeTeam().getId();
        dto.awayTeamId = m.getAwayTeam().getId();
        dto.homeScore = m.getHomeScore();
        dto.awayScore = m.getAwayScore();
        dto.status = m.getStatus().name();
        dto.stadium = m.getStadium();
        dto.matchDate = m.getMatchDate().toString();
        return dto;
    }
}
