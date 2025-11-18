package com.example.premier_league.service;

import com.example.premier_league.entity.Player;
import com.example.premier_league.repository.IPlayerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlayerService implements IPlayerService {

    private final IPlayerRepository playerRepository;

    public PlayerService(IPlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Override
    public List<Player> findByTeamId(Long teamId) {
        return List.of();
    }

    @Override
    public List<Player> findAllByIds(List<Long> playerIds) {
        return List.of();
    }

    @Override
    public List<Player> findAll() {
        return playerRepository.findAll();
    }

    @Override
    public void save(Player player) {
        playerRepository.save(player);
    }

    @Override
    public Player findById(Long id) {
        return playerRepository.findById(id).orElse(null);
    }

    @Override
    public List<Player> findByName(String name) {
        return List.of();
    }

//    @Override
//    public List<Player> findByName(String name) {
//        return playerRepository.findByTeamId(name);
//    }

    @Override
    public void update(Player player) {
        if (playerRepository.existsById(player.getId())) {
            playerRepository.save(player);
        }
    }

    @Override
    public void delete(Long id) {
        if (playerRepository.existsById(id)) {
            playerRepository.deleteById(id);
        }
    }
}
