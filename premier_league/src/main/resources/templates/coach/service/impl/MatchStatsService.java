package com.example.premier_league.service.impl;

import com.example.premier_league.dto.MatchStatsDto;
import com.example.premier_league.entity.Match;
import com.example.premier_league.entity.MatchEvent;
import com.example.premier_league.repository.IMatchEventRepository;
import com.example.premier_league.repository.IMatchRepository;
import com.example.premier_league.service.IMatchStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchStatsService implements IMatchStatsService {

    private final IMatchEventRepository eventRepo;
    private final IMatchRepository matchRepo;

    @Override
    public MatchStatsDto getStats(Long matchId) {

        Match match = matchRepo.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found: " + matchId));

        Long homeId = match.getHomeTeam().getId();
        Long awayId = match.getAwayTeam().getId();

        List<MatchEvent> events = eventRepo.findByMatchIdOrderByMinuteAsc(matchId);

        int shotsHome = 0, shotsAway = 0;
        int shotsOnTargetHome = 0, shotsOnTargetAway = 0;
        int foulsHome = 0, foulsAway = 0;

        int possessionHome = 0, possessionAway = 0;

        for (MatchEvent ev : events) {

            Long teamId = ev.getTeamId();

            boolean isHome = teamId != null && teamId.equals(homeId);
            boolean isAway = teamId != null && teamId.equals(awayId);

            String type = ev.getType();

            switch (type) {

                case "GOAL":
                case "SHOT":
                    if (isHome) shotsHome++;
                    else if (isAway) shotsAway++;
                    break;

                case "SHOT_ON_TARGET":
                case "PENALTY":
                    if (isHome) shotsOnTargetHome++;
                    else if (isAway) shotsOnTargetAway++;
                    break;

                case "YELLOW_CARD":
                case "RED_CARD":
                case "FOUL":
                    if (isHome) foulsHome++;
                    else if (isAway) foulsAway++;
                    break;

                default:
                    break;
            }

            // tính kiểm soát bóng
            if (teamId != null) {
                if (isHome) possessionHome++;
                if (isAway) possessionAway++;
            }
        }

        // Phần trăm kiểm soát
        int total = possessionHome + possessionAway;
        int homePercent = (total == 0 ? 50 : possessionHome * 100 / total);
        int awayPercent = 100 - homePercent;

        return MatchStatsDto.builder()
                .shotsHome(shotsHome)
                .shotsAway(shotsAway)
                .shotsOnTargetHome(shotsOnTargetHome)
                .shotsOnTargetAway(shotsOnTargetAway)
                .foulsHome(foulsHome)
                .foulsAway(foulsAway)
                .possessionHome(homePercent)
                .possessionAway(awayPercent)
                .build();
    }
}
