package com.example.premier_league.repository;

import com.example.premier_league.entity.MatchSchedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    List<MatchSchedule> findByIdNot(Long id);

    @Query("""
            SELECT m FROM MatchSchedule m
            WHERE (:team IS NULL OR LOWER(m.homeTeam.name) LIKE LOWER(CONCAT('%', :team, '%'))
                   OR LOWER(m.awayTeam.name) LIKE LOWER(CONCAT('%', :team, '%')))
              AND (:date IS NULL OR m.matchDate = :date)
              AND (:round IS NULL OR m.round = :round)
            ORDER BY m.matchDate ASC, m.matchTime ASC
            """)
    Page<MatchSchedule> search(
            @Param("team") String team,
            @Param("date") LocalDate date,
            @Param("round") Integer round,
            Pageable pageable
    );
}
