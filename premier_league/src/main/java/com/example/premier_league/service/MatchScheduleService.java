package com.example.premier_league.service;

import com.example.premier_league.entity.MatchSchedule;
import com.example.premier_league.entity.MatchStatus;
import com.example.premier_league.repository.IMatchScheduleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.example.premier_league.dto.CoachMatchScheduleDto;
import com.example.premier_league.repository.IMatchLineupRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MatchScheduleService implements IMatchScheduleService {

    private final IMatchScheduleRepository matchScheduleRepository;

    private final IMatchLineupRepository iMatchLineupRepository; //Thới

    public MatchScheduleService(IMatchScheduleRepository matchScheduleRepository, IMatchLineupRepository iMatchLineupRepository) {
        this.matchScheduleRepository = matchScheduleRepository;
        this.iMatchLineupRepository = iMatchLineupRepository; //THới
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

    @Override
    public List<MatchSchedule> findMatchesByTeamId(Long teamId) {
        // Sử dụng method đã có sẵn trong repository
        return matchScheduleRepository.findAllByHomeTeamIdOrAwayTeamIdOrderByMatchDateAscMatchTimeAsc(teamId, teamId);
    }

    @Override
    public List<CoachMatchScheduleDto> getCoachMatchSchedules(Long teamId) {
        List<MatchSchedule> matches = this.findMatchesByTeamId(teamId);

        return matches.stream().map(match -> {
            // Kiểm tra xem đã đăng ký đội hình cho trận này chưa
            boolean isRegistered = iMatchLineupRepository.existsByMatchIdAndTeamId(match.getId(), teamId);
            return new CoachMatchScheduleDto(match, isRegistered);
        }).collect(Collectors.toList());
    }
}
