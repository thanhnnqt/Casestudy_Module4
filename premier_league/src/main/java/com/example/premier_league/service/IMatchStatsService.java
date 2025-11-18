package com.example.premier_league.service;

import com.example.premier_league.dto.MatchStatsDto;
import org.springframework.stereotype.Service;

public interface IMatchStatsService {
    MatchStatsDto getStats(Long matchId);
}
