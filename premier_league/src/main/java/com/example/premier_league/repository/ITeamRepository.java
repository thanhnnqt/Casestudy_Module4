package com.example.premier_league.repository;

import com.example.premier_league.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ITeamRepository extends JpaRepository<Team, Integer> {
}
