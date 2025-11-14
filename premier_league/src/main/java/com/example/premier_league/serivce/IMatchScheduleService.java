package com.example.premier_league.serivce;

import com.example.premier_league.entity.MatchSchedule;

import java.util.List;

public interface IMatchScheduleService {
    List<MatchSchedule> getAllMatches();
    MatchSchedule save(MatchSchedule matchSchedule);
    MatchSchedule postponeMatch(Long id);
    MatchSchedule findById(Long id);
    MatchSchedule resumeMatch(Long id);
}
