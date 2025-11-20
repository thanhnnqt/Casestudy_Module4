package com.example.premier_league.service;

import com.example.premier_league.dto.MatchStatsDto;



public interface IMatchStatsService {

        MatchStatsDto getStatsByMatchId(Long matchId);

        MatchStatsDto updateStats(Long matchId, MatchStatsDto dto);

}

