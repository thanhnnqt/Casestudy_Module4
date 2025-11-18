package com.example.premier_league.service;

import com.example.premier_league.entity.Player;

import java.util.List;

public interface IPlayerService {
    List<Player> findByTeamId(Long teamId);
    Player findById(Long playerId);
    List<Player> findAllByIds(List<Long> playerIds);
    List<Player> findAll();
}