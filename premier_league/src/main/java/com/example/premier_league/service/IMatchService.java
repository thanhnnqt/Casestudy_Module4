package com.example.premier_league.service;

import com.example.premier_league.entity.Match;

public interface IMatchService {
    Match findById(Long id);

    void save(Match match);
}
