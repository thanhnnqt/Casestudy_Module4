package com.example.premier_league.repository;

import com.example.premier_league.entity.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ITournamentRepository extends JpaRepository<Tournament, Long> {
}
