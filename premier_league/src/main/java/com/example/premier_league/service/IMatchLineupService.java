package com.example.premier_league.service;

import com.example.premier_league.entity.MatchLineup;

import java.util.List;

public interface IMatchLineupService {
    List<MatchLineup> findByMatchAndTeam(Long matchId, Long teamId);

    /**
     * Lưu đội hình
     * @param teamId ID của đội HLV đang quản lý
     * @param matchId ID của trận đấu
     * @param mainPlayerIds Danh sách ID cầu thủ đá chính
     * @param subPlayerIds Danh sách ID cầu thủ dự bị
     */
    void saveLineup(Long teamId, Long matchId, List<Long> mainPlayerIds, List<Long> subPlayerIds);
}