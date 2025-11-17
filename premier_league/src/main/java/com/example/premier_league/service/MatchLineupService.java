package com.example.premier_league.service;

import com.example.premier_league.entity.*;
import com.example.premier_league.repository.IMatchLineupRepository;
import com.example.premier_league.repository.IMatchScheduleRepository;
import com.example.premier_league.repository.IPlayerRepository;
import com.example.premier_league.repository.ITeamRepository;
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
    public void saveLineup(Long teamId, Long matchId, List<Long> mainPlayerIds, List<Long> subPlayerIds) {
        // 1. Xóa đội hình cũ của đội này cho trận này
        iMatchLineupRepository.deleteByMatchIdAndTeamId(matchId, teamId);

        // 2. Lấy các đối tượng tham chiếu (để tránh query N+1)
        Team team = iTeamRepository.getReferenceById(teamId);
        MatchSchedule match = iMatchScheduleRepository.getReferenceById(matchId);

        List<MatchLineup> newLineups = new ArrayList<>();

        // 3. Tạo đội hình chính
        for (Long playerId : mainPlayerIds) {
            Player player = iPlayerRepository.getReferenceById(playerId);
            MatchLineup lineup = MatchLineup.builder()
                    .match(match)
                    .team(team)
                    .player(player)
                    .type(MatchLineup.LineupType.MAIN) // <-- Đã sửa ở đây
                    .build();
            newLineups.add(lineup);
        }

        // 4. Tạo đội hình dự bị
        for (Long playerId : subPlayerIds) {
            Player player = iPlayerRepository.getReferenceById(playerId);
            MatchLineup lineup = MatchLineup.builder()
                    .match(match)
                    .team(team)
                    .player(player)
                    .type(MatchLineup.LineupType.SUB) // <-- Đã sửa ở đây
                    .build();
            newLineups.add(lineup);
        }

        // 5. Lưu tất cả
        iMatchLineupRepository.saveAll(newLineups);
    }
}