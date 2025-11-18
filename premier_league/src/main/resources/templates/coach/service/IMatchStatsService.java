package com.example.premier_league.service;

import com.example.premier_league.dto.MatchStatsDto;

public interface IMatchStatsService {
    MatchStatsDto getStats(Long matchId);
}
