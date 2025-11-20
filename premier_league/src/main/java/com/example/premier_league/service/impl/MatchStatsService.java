package com.example.premier_league.service.impl;

import com.example.premier_league.dto.MatchStatsDto;
import com.example.premier_league.entity.MatchStats;
import com.example.premier_league.repository.IMatchStatsRepository;
import com.example.premier_league.service.IMatchStatsService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MatchStatsService implements IMatchStatsService {

    private final IMatchStatsRepository statsRepo;
    private final SimpMessagingTemplate messaging;

    @Override
    public MatchStatsDto getStatsByMatchId(Long matchId) {
        return statsRepo.findByMatchId(matchId)
                .map(this::toDto)
                .orElse(new MatchStatsDto());
    }

    @Override
    @Transactional
    public MatchStatsDto updateStats(Long matchId, MatchStatsDto dto) {

        MatchStats stats = statsRepo.findByMatchId(matchId)
                .orElse(new MatchStats(matchId));

        // --- SET GIÁ TRỊ ---
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
        stats.setYellowCardsHome(dto.getYellowCardsHome());
        stats.setYellowCardsAway(dto.getYellowCardsAway());
        stats.setRedCardsHome(dto.getRedCardsHome());
        stats.setRedCardsAway(dto.getRedCardsAway());
        stats.setOffsidesHome(dto.getOffsidesHome());
        stats.setOffsidesAway(dto.getOffsidesAway());
        stats.setCornersHome(dto.getCornersHome());
        stats.setCornersAway(dto.getCornersAway());

        MatchStats saved = statsRepo.save(stats);

        // 🔥 Gửi realtime FE
        messaging.convertAndSend("/topic/match/" + matchId + "/stats", toDto(saved));

        return toDto(saved);
    }


    // ==========================
    // HÀM toDto
    // ==========================
    private MatchStatsDto toDto(MatchStats s) {
        MatchStatsDto dto = new MatchStatsDto();

        dto.setShotsHome(s.getShotsHome());
        dto.setShotsAway(s.getShotsAway());
        dto.setShotsOnTargetHome(s.getShotsOnTargetHome());
        dto.setShotsOnTargetAway(s.getShotsOnTargetAway());
        dto.setPossessionHome(s.getPossessionHome());
        dto.setPossessionAway(s.getPossessionAway());
        dto.setPassesHome(s.getPassesHome());
        dto.setPassesAway(s.getPassesAway());
        dto.setAccuracyHome(s.getAccuracyHome());
        dto.setAccuracyAway(s.getAccuracyAway());
        dto.setFoulsHome(s.getFoulsHome());
        dto.setFoulsAway(s.getFoulsAway());
        dto.setYellowCardsHome(s.getYellowCardsHome());
        dto.setYellowCardsAway(s.getYellowCardsAway());
        dto.setRedCardsHome(s.getRedCardsHome());
        dto.setRedCardsAway(s.getRedCardsAway());
        dto.setOffsidesHome(s.getOffsidesHome());
        dto.setOffsidesAway(s.getOffsidesAway());
        dto.setCornersHome(s.getCornersHome());
        dto.setCornersAway(s.getCornersAway());

        return dto;
    }

}

