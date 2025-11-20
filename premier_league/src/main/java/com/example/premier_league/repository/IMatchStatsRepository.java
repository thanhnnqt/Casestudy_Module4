package com.example.premier_league.repository;

import com.example.premier_league.entity.MatchStats;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IMatchStatsRepository extends JpaRepository<MatchStats, Long> {

    Optional<MatchStats> findByMatchId(Long matchId);


}
