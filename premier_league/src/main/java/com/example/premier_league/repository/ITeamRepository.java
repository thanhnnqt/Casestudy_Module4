package com.example.premier_league.repository;

import com.example.premier_league.entity.Staff;
import com.example.premier_league.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ITeamRepository extends JpaRepository<Team, Long> {
}
