package com.example.premier_league.service;

import com.example.premier_league.dto.PlayerDto;
import com.example.premier_league.dto.PlayerShortDto;
import com.example.premier_league.entity.Player;

import java.util.List;

public interface IPlayerService {
    List<Player> findByTeamId(Long teamId);
    List<Player> findAllByIds(List<Long> playerIds);

    List<Player> findAll();

    void save(Player player);

    Player findById(Long id);

    List<Player> findByName(String name);

    void update(Player player);

    void delete(Long id);

    List<PlayerShortDto> getPlayersByTeam(Long teamId);}