package com.example.premier_league.service;

import com.example.premier_league.entity.MatchSchedule;
import com.example.premier_league.entity.MatchStatus;
import com.example.premier_league.repository.IMatchScheduleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
public class MatchScheduleService implements IMatchScheduleService {

    private final IMatchScheduleRepository matchScheduleRepository;

    public MatchScheduleService(IMatchScheduleRepository matchScheduleRepository) {
        this.matchScheduleRepository = matchScheduleRepository;
    }

    @Override
    public Page<MatchSchedule> getAllMatches(Pageable pageable) {
        return matchScheduleRepository.findAllByOrderByMatchDateAscMatchTimeAsc(pageable);
    }

    @Override
    public MatchSchedule save(MatchSchedule matchSchedule) {
        return null;
    }

    @Override
    public MatchSchedule postponeMatch(Long id) {
        return null;
    }

    @Override
    public Page<MatchSchedule> searchByTeam(String team, Pageable pageable) {
        return matchScheduleRepository
                .findByHomeTeam_NameContainingIgnoreCaseOrAwayTeam_NameContainingIgnoreCase(
                        team, team, pageable);
    }

    @Override
    public Page<MatchSchedule> searchByDate(LocalDate date, Pageable pageable) {
        return matchScheduleRepository.findByMatchDate(date, pageable);
    }

    @Override
    public Page<MatchSchedule> searchByRound(Integer round, Pageable pageable) {
        return matchScheduleRepository.findByRound(round, pageable);
    }

    @Override
    public MatchSchedule findById(Long id) {
        return matchScheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Match not found"));
    }

    @Override
    public MatchSchedule resumeMatch(Long id) {
        return null;
    }

    @Override
    public void updateStatus(Long id, MatchStatus status) {
        MatchSchedule match = findById(id);
        match.setStatus(status);
        matchScheduleRepository.save(match);
    }

    @Override
    public void reschedule(Long id, LocalDate newDate, String newTime) {
        MatchSchedule match = findById(id);
        match.setMatchDate(newDate);
        match.setMatchTime(LocalTime.parse(newTime));
        match.setStatus(MatchStatus.SCHEDULED);
        matchScheduleRepository.save(match);
    }
}
