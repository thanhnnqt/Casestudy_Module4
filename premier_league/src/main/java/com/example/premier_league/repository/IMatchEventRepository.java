package com.example.premier_league.repository;

import com.example.premier_league.entity.MatchEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IMatchEventRepository extends JpaRepository<MatchEvent, Long> {
    List<MatchEvent> findByMatchIdOrderByCreatedAtAsc(Long matchId);

    List<MatchEvent> findByMatchIdOrderByMinuteAsc(Long matchId);
}
