package com.example.premier_league.service;

import com.example.premier_league.entity.MatchSchedule;
import com.example.premier_league.entity.MatchStatus;
import com.example.premier_league.repository.IMatchScheduleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class MatchScheduleService implements IMatchScheduleService {

    private final IMatchScheduleRepository matchScheduleRepository;

    public MatchScheduleService(IMatchScheduleRepository matchScheduleRepository) {
        this.matchScheduleRepository = matchScheduleRepository;
    }


    /* ================= FETCH DATA ================= */

    @Override
    public Page<MatchSchedule> getAllMatches(Pageable pageable) {

        Page<MatchSchedule> page = matchScheduleRepository
                .findAllByOrderByMatchDateAscMatchTimeAsc(pageable);

        // ✔ Update trạng thái tự động theo ngày mỗi lần list
        page.forEach(this::autoUpdateStatus);

        return page;
    }

    @Override
    public Page<MatchSchedule> searchByTeam(String team, Pageable pageable) {
        Page<MatchSchedule> page =
                matchScheduleRepository.findByHomeTeam_NameContainingIgnoreCaseOrAwayTeam_NameContainingIgnoreCase(
                        team, team, pageable
                );

        page.forEach(this::autoUpdateStatus);
        return page;
    }

    @Override
    public Page<MatchSchedule> searchByDate(LocalDate date, Pageable pageable) {
        Page<MatchSchedule> page = matchScheduleRepository.findByMatchDate(date, pageable);
        page.forEach(this::autoUpdateStatus);
        return page;
    }

    @Override
    public Page<MatchSchedule> searchByRound(Integer round, Pageable pageable) {
        Page<MatchSchedule> page = matchScheduleRepository.findByRound(round, pageable);
        page.forEach(this::autoUpdateStatus);
        return page;
    }

    @Override
    public MatchSchedule findById(Long id) {
        MatchSchedule match = matchScheduleRepository.findById(id).orElseThrow();
        autoUpdateStatus(match);
        return match;
    }



    /* ================= CREATE / SAVE ================= */

    @Override
    public MatchSchedule save(MatchSchedule matchSchedule) {
        autoUpdateStatus(matchSchedule);
        return matchScheduleRepository.save(matchSchedule);
    }



    /* ================= STATUS ACTIONS ================= */

    @Override
    public void updateStatus(Long id, MatchStatus status) {
        MatchSchedule match = findById(id);
        match.setStatus(status);
        matchScheduleRepository.save(match);
    }

    @Override
    public MatchSchedule postponeMatch(Long id) {
        MatchSchedule match = findById(id);
        match.setStatus(MatchStatus.POSTPONED);
        return matchScheduleRepository.save(match);
    }

    @Override
    public MatchSchedule resumeMatch(Long id) {
        MatchSchedule match = findById(id);
        match.setStatus(MatchStatus.SCHEDULED);
        return matchScheduleRepository.save(match);
    }

    @Override
    public List<MatchSchedule> findMatchesByTeamId(Long teamId) {
        return List.of();
    }



    /* ================= RESCHEDULE ================= */

    @Override
    public void reschedule(Long id, LocalDate newDate, String newTime) {
        MatchSchedule match = findById(id);

        match.setMatchDate(newDate);
        match.setMatchTime(LocalTime.parse(newTime));

        // khi dời lịch thì bỏ trạng thái POSTPONED
        autoUpdateStatus(match);  // cập nhật UPCOMING hoặc SCHEDULED

        matchScheduleRepository.save(match);
    }



    /* ================= AUTO STATUS BY DATE ================= */

    /**
     * ✔ Nếu còn >= 2 ngày -> UPCOMING
     * ✔ Nếu còn < 2 ngày -> SCHEDULED
     * ✔ Trận đã qua, POSTPONED, LIVE, FINISHED -> giữ nguyên
     */
    private void autoUpdateStatus(MatchSchedule match) {

        // Không update nếu match đang LIVE, POSTPONED, FINISHED
        if (match.getStatus() == MatchStatus.POSTPONED ||
                match.getStatus() == MatchStatus.LIVE ||
                match.getStatus() == MatchStatus.FINISHED) {
            return;
        }

        LocalDate today = LocalDate.now();
        LocalDate matchDate = match.getMatchDate();

        long daysLeft = ChronoUnit.DAYS.between(today, matchDate);

        if (daysLeft >= 2) {
            match.setStatus(MatchStatus.UPCOMING);
        } else if (daysLeft < 2 && daysLeft >= 0) {
            match.setStatus(MatchStatus.SCHEDULED);
        }
    }
}
