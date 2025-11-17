package com.example.premier_league.service;

import com.example.premier_league.entity.MatchSchedule;
import com.example.premier_league.entity.MatchStatus;
import com.example.premier_league.repository.IMatchScheduleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MatchScheduleService implements IMatchScheduleService {
    private final IMatchScheduleRepository matchScheduleRepository;

    public MatchScheduleService(IMatchScheduleRepository matchScheduleRepository) {
        this.matchScheduleRepository = matchScheduleRepository;
    }

    @Override
    public List<MatchSchedule> getAllMatches() {
        return matchScheduleRepository.findAllByOrderByMatchDateAscMatchTimeAsc();
    }

    @Override
    public MatchSchedule save(MatchSchedule matchSchedule) {
        return matchScheduleRepository.save(matchSchedule);
    }

    @Override
    public MatchSchedule postponeMatch(Long id) {
        MatchSchedule match = matchScheduleRepository.findById(id).orElseThrow();
        match.setStatus(MatchStatus.POSTPONED);
        return matchScheduleRepository.save(match);
    }

    @Override
    public MatchSchedule findById(Long id) {
        return matchScheduleRepository.findById(id).orElseThrow(() -> new RuntimeException("Match not found"));
    }

    public MatchSchedule resumeMatch(Long id) {
        MatchSchedule match = findById(id);
        match.setStatus(MatchStatus.SCHEDULED);
        return matchScheduleRepository.save(match);
    }

    @Override
    public List<MatchSchedule> findMatchesByTeamId(Long teamId) {
        // Sử dụng method đã có sẵn trong repository
        return matchScheduleRepository.findAllByHomeTeamIdOrAwayTeamIdOrderByMatchDateAscMatchTimeAsc(teamId, teamId);
    }
}
