package com.example.premier_league.service;

import com.example.premier_league.dto.CoachMatchScheduleDto;
import com.example.premier_league.entity.MatchSchedule;
import com.example.premier_league.entity.MatchStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface IMatchScheduleService {
    Page<MatchSchedule> getAllMatches(Pageable pageable);
    MatchSchedule save(MatchSchedule matchSchedule);
    MatchSchedule postponeMatch(Long id);
    MatchSchedule findById(Long id);
    MatchSchedule resumeMatch(Long id);
    List<MatchSchedule> findMatchesByTeamId(Long teamId); //Thới bổ sung
    public Page<MatchSchedule> searchByTeam(String team, Pageable pageable);
    Page<MatchSchedule> searchByDate(LocalDate date, Pageable pageable);
    Page<MatchSchedule> searchByRound(Integer round, Pageable pageable);
    void updateStatus(Long id, MatchStatus status);
    void reschedule(Long id, LocalDate newDate, String newTime);
    List<CoachMatchScheduleDto> getCoachMatchSchedules(Long teamId);//THới
}
