package com.example.premier_league.service.impl;

import com.example.premier_league.dto.MatchStatsDto;
import com.example.premier_league.entity.MatchStats;
import com.example.premier_league.repository.IMatchStatsRepository;
import com.example.premier_league.service.IMatchStatsService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MatchStatsService implements IMatchStatsService {

    private final IMatchStatsRepository repo;

    @Override
    public MatchStats getStats(Long matchId) {
        return repo.findByMatchId(matchId)
                .orElseGet(() -> repo.save(
                        MatchStats.builder()
                                .matchId(matchId)
                                .build()
                ));
    }

    @Override
    @Transactional
    public MatchStats updateStats(Long matchId, MatchStatsDto dto) {

        MatchStats stats = getStats(matchId);

        stats.setShotsHome(dto.getShotsHome());
        stats.setShotsAway(dto.getShotsAway());

        stats.setShotsOnTargetHome(dto.getShotsOnTargetHome());
        stats.setShotsOnTargetAway(dto.getShotsOnTargetAway());

        stats.setPossessionHome(dto.getPossessionHome());
        stats.setPossessionAway(dto.getPossessionAway());

        stats.setPassesHome(dto.getPassesHome());
        stats.setPassesAway(dto.getPassesAway());

        stats.setAccuracyHome(dto.getAccuracyHome());
        stats.setAccuracyAway(dto.getAccuracyAway());

        stats.setFoulsHome(dto.getFoulsHome());
        stats.setFoulsAway(dto.getFoulsAway());

        return repo.save(stats);
    }
}
