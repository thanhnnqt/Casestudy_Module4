package com.example.premier_league.service;

import com.example.premier_league.dto.MatchStatsDto;
import com.example.premier_league.entity.MatchStats;


public interface IMatchStatsService {
    MatchStats getStats(Long matchId);
    MatchStats updateStats(Long matchId, MatchStatsDto dto);}
