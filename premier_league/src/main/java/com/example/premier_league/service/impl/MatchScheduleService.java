package com.example.premier_league.service.impl;

import com.example.premier_league.entity.MatchSchedule;
import com.example.premier_league.entity.MatchStatus;
import com.example.premier_league.repository.IMatchScheduleRepository;
import com.example.premier_league.service.IMatchScheduleService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.example.premier_league.dto.CoachMatchScheduleDto;
import com.example.premier_league.repository.IMatchLineupRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
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


    /* ================= FETCH DATA ================= */

    @Override
    public List<MatchSchedule> findAll() {
        return matchScheduleRepository.findAll();
    }

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
        // SỬA 1: Thay vì "return List.of()", hãy gọi repository
        List<MatchSchedule> matches = matchScheduleRepository
                .findAllByHomeTeamIdOrAwayTeamIdOrderByMatchDateAscMatchTimeAsc(teamId, teamId);

        // SỬA 2: Thêm autoUpdateStatus cho các trận này
        matches.forEach(this::autoUpdateStatus);

        return matches;
    }



    /* ================= RESCHEDULE ================= */

//    @Override
//    public void reschedule(Long id, LocalDate newDate, String newTime) {
//        MatchSchedule match = findById(id);
//
//        match.setMatchDate(newDate);
//        match.setMatchTime(LocalTime.parse(newTime));
//
//        // khi dời lịch thì bỏ trạng thái POSTPONED
//        autoUpdateStatus(match);  // cập nhật UPCOMING hoặc SCHEDULED
//
//        matchScheduleRepository.save(match);
//    }



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

    @Override
    public List<CoachMatchScheduleDto> getCoachMatchSchedules(Long teamId) {
        List<MatchSchedule> matches = this.findMatchesByTeamId(teamId);

        return matches.stream().map(match -> {
            // Kiểm tra xem đã đăng ký đội hình cho trận này chưa
            boolean isRegistered = iMatchLineupRepository.existsByMatchIdAndTeamId(match.getId(), teamId);
            return new CoachMatchScheduleDto(match, isRegistered);
        }).collect(Collectors.toList());
    }

    @Override
    public void reschedule(Long id, LocalDate newDate, String newTime) {
        if (newDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Ngày dời lịch không được nhỏ hơn ngày hiện tại!");
        }
        MatchSchedule match = findById(id);

        // Lấy id 2 đội của trận này
        Long homeTeamId = match.getHomeTeam() != null ? match.getHomeTeam().getId() : null;
        Long awayTeamId = match.getAwayTeam() != null ? match.getAwayTeam().getId() : null;

        // 1) Nếu không có team id (dữ liệu hỏng) -> ném lỗi
        if (homeTeamId == null && awayTeamId == null) {
            throw new IllegalStateException("Trận đấu chưa có đội tham gia, không thể dời lịch.");
        }

        // 2) Lấy tất cả trận có liên quan tới 2 đội (home OR away)
        List<MatchSchedule> relatedMatches = matchScheduleRepository
                .findAllByHomeTeamIdOrAwayTeamIdOrderByMatchDateAscMatchTimeAsc(homeTeamId, awayTeamId);

        // 3) Duyệt và validate (bỏ qua chính trận và các trận đã POSTPONED/FINISHED)
        for (MatchSchedule m : relatedMatches) {
            if (m.getId().equals(id)) {
                continue; // bỏ qua chính trận
            }
            if (m.getStatus() == MatchStatus.POSTPONED || m.getStatus() == MatchStatus.FINISHED) {
                continue; // bỏ qua nếu không còn ràng buộc
            }

            LocalDate blockStart = m.getMatchDate().minusDays(2);
            LocalDate blockEnd   = m.getMatchDate().plusDays(2);

            boolean isInsideBlockedRange =
                    ( !newDate.isBefore(blockStart) ) && ( !newDate.isAfter(blockEnd) );

            if (isInsideBlockedRange) {
                throw new IllegalArgumentException(
                        "Ngày mới không hợp lệ! Phải cách trận '" + m.getHomeTeam().getName()
                                + " vs " + m.getAwayTeam().getName()
                                + "' diễn ra ngày " + m.getMatchDate()
                                + " ít nhất 2 ngày."
                );
            }
        }

        // 4) Nếu hợp lệ -> cập nhật
        match.setMatchDate(newDate);
        match.setMatchTime(LocalTime.parse(newTime));
        match.setStatus(MatchStatus.UPCOMING);

        autoUpdateStatus(match);
        matchScheduleRepository.save(match);
    }
    @Override
    public boolean hasSchedule() {
        return matchScheduleRepository.count() > 0;
    }

    @Override
    public Page<MatchSchedule> search(String team, LocalDate date, Integer round, Pageable pageable) {
        Page<MatchSchedule> page = matchScheduleRepository.search(team, date, round, pageable);
        page.forEach(this::autoUpdateStatus);
        return page;
    }
}
