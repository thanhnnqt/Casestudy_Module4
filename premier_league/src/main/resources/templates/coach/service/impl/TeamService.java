package com.example.premier_league.service.impl;

import com.example.premier_league.entity.Team;
import com.example.premier_league.repository.ITeamRepository;
import com.example.premier_league.service.ITeamService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeamService implements ITeamService {

    private final ITeamRepository teamRepository;

    public TeamService(ITeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    @Override
    public List<Team> findAll() {
        return teamRepository.findAll();
    }

    @Override
    public void save(Team team) {
        teamRepository.save(team);
    }

    @Override
    public Team findById(Long id) {
        return teamRepository.findById(id).orElse(null);
    }

    @Override
    public List<Team> findByName(String name) {
        return List.of();
    }

    @Override
    public void update(Team team) {

        if (teamRepository.existsById(team.getId())) {
            teamRepository.save(team);
        }
    }

    @Override
    public void delete(Long id) {
        if (teamRepository.existsById(id)) {
            teamRepository.deleteById(id);
        }
    }
}
