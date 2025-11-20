package com.example.premier_league.service.impl;

import com.example.premier_league.dto.PlayerShortDto;
import com.example.premier_league.entity.Player;
import com.example.premier_league.repository.IPlayerRepository;
import com.example.premier_league.service.IPlayerService;
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

        return playerRepository.findByTeamId(teamId);
    }

    @Override
    public List<Player> findAllByIds(List<Long> playerIds) {
        return playerRepository.findAllById(playerIds);
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


    @Override
    public List<PlayerShortDto> getPlayersByTeam(Long teamId) {
        List<Player> players = playerRepository.findByTeamId(teamId);

        return players.stream()
                .map(p -> new PlayerShortDto(
                        p.getId(),
                        p.getName(),
                        p.getPosition(),
                        p.getAvatar(),
                        p.getSeasonYellowCards(),         // Thẻ vàng mùa giải
                        p.getSuspensionMatchesRemaining() // Số trận treo giò
                ))
                .toList();
    }
}


