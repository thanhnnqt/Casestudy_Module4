package com.example.premier_league.repository;

import com.example.premier_league.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IPlayerRepository extends JpaRepository<Player, Long> {
    /**
     * Lấy danh sách cầu thủ của một đội
     */
    List<Player> findByTeamId(Long teamId);
}