package com.example.premier_league.service;

import com.example.premier_league.dto.RankingDto;
import com.example.premier_league.entity.Team;
import com.example.premier_league.repository.ITeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RankingService {
    private final ITeamRepository teamRepository;


    public List<RankingDto> getRanking() {
        return teamRepository.findAll().stream()
                .map(t -> {
                    RankingDto dto = new RankingDto();
                    dto.teamName = t.getName();
                    dto.logoUrl = t.getLogoUrl();
                    dto.points = t.getPoints();
                    dto.goalsFor = t.getGoalsFor();
                    dto.goalsAgainst = t.getGoalsAgainst();
                    dto.goalDifference = t.getGoalDifference();
                    return dto;
                })
                .sorted((a, b) -> {
                    if (b.points != a.points) return b.points - a.points;
                    return b.goalDifference - a.goalDifference;
                })
                .toList();
    }

    public void delete(Long id) {
        teamRepository.deleteById(id);
    }

    public Team findById(Long id) {
        return teamRepository.findById(id).orElse(null);
    }

    public void save(Team teamUpdate) {
        teamRepository.save(teamUpdate);
    }
}
