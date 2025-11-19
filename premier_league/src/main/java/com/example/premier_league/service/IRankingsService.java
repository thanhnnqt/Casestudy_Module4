package com.example.premier_league.service;

import com.example.premier_league.dto.RankingsDto;
import com.example.premier_league.entity.Match;
import com.example.premier_league.entity.Team;

import java.util.List;

public interface IRankingsService {
    List<RankingsDto> getRanking();
    void applyMatchResult(Match match);
    Team findById(Long id);
    void save(Team teamUpdate);
    void delete(Long id);
}

