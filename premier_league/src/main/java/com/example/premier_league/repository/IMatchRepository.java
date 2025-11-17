package com.example.premier_league.repository;

import com.example.premier_league.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IMatchRepository extends JpaRepository<Match, Long> {
}
