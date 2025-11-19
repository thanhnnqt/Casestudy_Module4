package com.example.premier_league.service.impl;

import com.example.premier_league.entity.*;
import com.example.premier_league.repository.*;
import com.example.premier_league.service.IMatchLineupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchLineupService implements IMatchLineupService {

    private final IMatchLineupRepository iMatchLineupRepository;
    private final ITeamRepository iTeamRepository;
    private final IMatchScheduleRepository iMatchScheduleRepository;
    private final IPlayerRepository iPlayerRepository;

    @Override
    public List<MatchLineup> findByMatchAndTeam(Long matchId, Long teamId) {
        return iMatchLineupRepository.findByMatchIdAndTeamId(matchId, teamId);
    }

    @Override
    @Transactional
    public void saveLineup(Long teamId, Long matchId, List<Long> mainPlayerIds, List<Long> subPlayerIds, Long captainId) {
        // 1. Xóa đội hình cũ
        iMatchLineupRepository.deleteByMatchIdAndTeamId(matchId, teamId);

        Team team = iTeamRepository.getReferenceById(teamId);
        MatchSchedule match = iMatchScheduleRepository.getReferenceById(matchId);
        List<MatchLineup> newLineups = new ArrayList<>();

        // 2. Lưu cầu thủ chính (kèm check đội trưởng)
        for (Long playerId : mainPlayerIds) {
            Player player = iPlayerRepository.getReferenceById(playerId);
            boolean isCap = (captainId != null && captainId.equals(playerId)); // Check captain

            newLineups.add(MatchLineup.builder()
                    .match(match)
                    .team(team)
                    .player(player)
                    .type(MatchLineup.LineupType.MAIN)
                    .isCaptain(isCap) // Lưu captain
                    .build());
        }

        // 3. Lưu cầu thủ dự bị
        for (Long playerId : subPlayerIds) {
            Player player = iPlayerRepository.getReferenceById(playerId);
            newLineups.add(MatchLineup.builder()
                    .match(match)
                    .team(team)
                    .player(player)
                    .type(MatchLineup.LineupType.SUB)
                    .isCaptain(false)
                    .build());
        }
        iMatchLineupRepository.saveAll(newLineups);
    }
}