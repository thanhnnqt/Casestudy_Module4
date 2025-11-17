package com.example.premier_league.service;


import com.example.premier_league.entity.Team;

import java.util.List;

public interface ITeamService {
    List<Team> findAll();

    void save (Team team);

    Team findById(Long id);

    List<Team> findByName(String name);

    void update(Team team);

    void delete(Long id);
}
