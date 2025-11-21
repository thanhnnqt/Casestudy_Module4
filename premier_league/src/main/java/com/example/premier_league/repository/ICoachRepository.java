package com.example.premier_league.repository;

import com.example.premier_league.entity.Coach;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ICoachRepository extends JpaRepository<Coach, Integer> {

    List<Coach> findByFullNameContainingIgnoreCase(String name);

    List<Coach> findByTeamId(Long teamId);

    boolean existsByRole(String role);
}
