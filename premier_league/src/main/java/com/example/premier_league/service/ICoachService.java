package com.example.premier_league.service;

import com.example.premier_league.entity.Coach;

import java.util.List;

public interface ICoachService {

    List<Coach> findAll();

    List<Coach> findByTeamId(Long teamId);

    void save(Coach coach);

    Coach findById(int id);

    List<Coach> findByName(String name);

    void update(Coach coach);

    void delete(int id);

    boolean existsByRole(String role);
}
