package com.example.premier_league.repository;

import com.example.premier_league.entity.MatchSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IMatchScheduleRepository extends JpaRepository<MatchSchedule, Long> {
    List<MatchSchedule> findAllByOrderByMatchDateAscMatchTimeAsc();
    List<MatchSchedule> findAllByHomeTeamIdOrAwayTeamIdOrderByMatchDateAscMatchTimeAsc(Long homeTeamId, Long awayTeamId); //tìm lịch thi đấu cho 1 đội (Nhà, khách)
}
