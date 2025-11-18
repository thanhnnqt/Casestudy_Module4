package com.example.premier_league.repository;

import com.example.premier_league.entity.MatchSchedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface IMatchScheduleRepository extends JpaRepository<MatchSchedule, Long> {

    List<MatchSchedule> findAllByOrderByMatchDateAscMatchTimeAsc();
    List<MatchSchedule> findAllByHomeTeamIdOrAwayTeamIdOrderByMatchDateAscMatchTimeAsc(Long homeTeamId, Long awayTeamId); //tìm lịch thi đấu cho 1 đội (Nhà, khách)

    Page<MatchSchedule> findAllByOrderByMatchDateAscMatchTimeAsc(Pageable pageable);

    Page<MatchSchedule> findByHomeTeam_NameContainingIgnoreCaseOrAwayTeam_NameContainingIgnoreCase(
            String homeTeam, String awayTeam, Pageable pageable
    );

    Page<MatchSchedule> findByMatchDate(LocalDate date, Pageable pageable);

    Page<MatchSchedule> findByRound(Integer round, Pageable pageable);

}
