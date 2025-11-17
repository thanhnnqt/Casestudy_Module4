package com.example.premier_league.repository;

import com.example.premier_league.entity.MatchLineup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IMatchLineupRepository extends JpaRepository<MatchLineup, Long> {
    /**
     * Lấy đội hình (cả chính và dự bị) của 1 đội cho 1 trận đấu
     */
    List<MatchLineup> findByMatchIdAndTeamId(Long matchId, Long teamId);

    /**
     * Xóa đội hình cũ trước khi lưu đội hình mới
     */
    void deleteByMatchIdAndTeamId(Long matchId, Long teamId);
}