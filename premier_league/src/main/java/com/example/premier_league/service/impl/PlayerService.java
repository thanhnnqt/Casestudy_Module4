package com.example.premier_league.service.impl;

import com.example.premier_league.entity.Player;
import com.example.premier_league.repository.IPlayerRepository;
import com.example.premier_league.service.IPlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlayerService implements IPlayerService {

    private final IPlayerRepository iPlayerRepository;

    @Override
    public List<Player> findByTeamId(Long teamId) {
        return iPlayerRepository.findByTeamId(teamId);
    }

    @Override
    public Player findById(Long playerId) {
        return iPlayerRepository.findById(playerId).orElse(null);
    }

    @Override
    public List<Player> findAllByIds(List<Long> playerIds) {
        return iPlayerRepository.findAllById(playerIds);
    }
}