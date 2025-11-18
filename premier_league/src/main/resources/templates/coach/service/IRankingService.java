package com.example.premier_league.service;

import com.example.premier_league.entity.Match;
import com.example.premier_league.entity.Team;

import java.util.List;

public interface IRankingService {
    void applyMatchResult(Match match);
    List<?> getRanking();

    Team findById(Long id);

    void save(Team teamUpdate);

    void delete(Long id);
}
