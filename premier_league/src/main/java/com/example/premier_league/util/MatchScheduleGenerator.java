package com.example.premier_league.util;

import com.example.premier_league.entity.MatchSchedule;
import com.example.premier_league.entity.Team;
import com.example.premier_league.repository.IMatchScheduleRepository;
import com.example.premier_league.repository.ITeamRepository;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class MatchScheduleGenerator {

    private final ITeamRepository teamRepository;
    private final IMatchScheduleRepository matchScheduleRepository;

    public MatchScheduleGenerator(IMatchScheduleRepository matchScheduleRepository,
                                  ITeamRepository teamRepository) {
        this.matchScheduleRepository = matchScheduleRepository;
        this.teamRepository = teamRepository;
    }

    /**
     * Tạo lịch thi đấu dựa trên ngày người dùng chọn
     */
    public void generateSchedule(LocalDate seasonStartDate) {

        if (matchScheduleRepository.count() > 0) {
            System.out.println("✔ Lịch thi đấu đã tồn tại — bỏ qua việc tạo mới.");
            return;
        }

        System.out.println("🔥 Đang tạo lịch thi đấu Premier League...");

        List<Team> teams = teamRepository.findAll();

        if (teams.size() != 20) {
            System.out.println("❌ Không đủ 20 đội — không thể tạo lịch.");
            return;
        }

        // Random thứ tự đội
        List<Team> teamList = new ArrayList<>(teams);
        Collections.shuffle(teamList);

        int totalRounds = teamList.size() - 1;   // 19 vòng
        int matchesPerRound = teamList.size() / 2; // 10 trận/vòng

        // Lấy cuối tuần gần nhất từ ngày người dùng chọn
        LocalDate startDate = nextWeekend(seasonStartDate);

        List<MatchSchedule> allMatches = new ArrayList<>();

        /* ====================== LƯỢT ĐI ======================= */
        for (int round = 0; round < totalRounds; round++) {

            List<LocalTime> saturdayTimes = generateMatchTimesSaturday();
            List<LocalTime> sundayTimes = generateMatchTimesSunday();

            for (int match = 0; match < matchesPerRound; match++) {

                Team home = teamList.get(match);
                Team away = teamList.get(teamList.size() - 1 - match);

                MatchSchedule m = new MatchSchedule();

                // 6 trận thứ 7 - 4 trận chủ nhật
                if (match < 6) {
                    m.setMatchDate(startDate.plusWeeks(round));
                    m.setMatchTime(saturdayTimes.get(match));
                } else {
                    m.setMatchDate(startDate.plusWeeks(round).plusDays(1));
                    m.setMatchTime(sundayTimes.get(match - 6));
                }

                m.setHomeTeam(home);
                m.setAwayTeam(away);
                m.setRound(round + 1);
                m.setName(home.getName() + " vs " + away.getName());

                allMatches.add(m);
            }

            // Xoay bảng (trừ đội đầu)
            teamList.add(1, teamList.remove(teamList.size() - 1));
        }

        /* ====================== LƯỢT VỀ ======================= */
        List<Team> reverseList = new ArrayList<>(teamList);
        Collections.shuffle(reverseList);

        for (int round = 0; round < totalRounds; round++) {

            List<LocalTime> saturdayTimes = generateMatchTimesSaturday();
            List<LocalTime> sundayTimes = generateMatchTimesSunday();

            for (int match = 0; match < matchesPerRound; match++) {

                Team home = reverseList.get(reverseList.size() - 1 - match);
                Team away = reverseList.get(match);

                MatchSchedule m = new MatchSchedule();

                if (match < 6) {
                    m.setMatchDate(startDate.plusWeeks(totalRounds + round));
                    m.setMatchTime(saturdayTimes.get(match));
                } else {
                    m.setMatchDate(startDate.plusWeeks(totalRounds + round).plusDays(1));
                    m.setMatchTime(sundayTimes.get(match - 6));
                }

                m.setHomeTeam(home);
                m.setAwayTeam(away);
                m.setRound(totalRounds + round + 1);
                m.setName(home.getName() + " vs " + away.getName());

                allMatches.add(m);
            }

            reverseList.add(1, reverseList.remove(reverseList.size() - 1));
        }

        matchScheduleRepository.saveAll(allMatches);
        System.out.println("🎉 Lịch thi đấu tạo thành công! Tổng số trận: " + allMatches.size());
    }

    /* =================== GIỜ THI ĐẤU THỨ 7 =================== */
    private List<LocalTime> generateMatchTimesSaturday() {
        List<LocalTime> times = new ArrayList<>();

        LocalTime[] saturdayTimes = {
                LocalTime.of(19, 30),
                LocalTime.of(21, 30),
                LocalTime.of(23, 30)
        };

        // mỗi giờ có 2 trận = 6 trận
        for (LocalTime t : saturdayTimes) {
            times.add(t);
            times.add(t);
        }

        Collections.shuffle(times);
        return times;
    }

    /* =================== GIỜ THI ĐẤU CHỦ NHẬT =================== */
    private List<LocalTime> generateMatchTimesSunday() {
        List<LocalTime> times = new ArrayList<>();

        LocalTime[] sundayTimes = {
                LocalTime.of(20, 0),
                LocalTime.of(22, 0)
        };

        // mỗi giờ 2 trận = 4 trận
        for (LocalTime t : sundayTimes) {
            times.add(t);
            times.add(t);
        }

        Collections.shuffle(times);
        return times;
    }

    /* ============= LẤY CUỐI TUẦN GẦN NHẤT ============= */
    private LocalDate nextWeekend(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();

        if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY)
            return date;

        int daysUntilSaturday =
                DayOfWeek.SATURDAY.getValue() - dow.getValue();

        return date.plusDays(daysUntilSaturday);
    }
}
