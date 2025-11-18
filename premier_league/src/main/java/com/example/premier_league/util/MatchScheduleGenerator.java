package com.example.premier_league.util;

import com.example.premier_league.entity.MatchSchedule;
import com.example.premier_league.entity.Team;
import com.example.premier_league.repository.IMatchScheduleRepository;
import com.example.premier_league.repository.ITeamRepository;
import jakarta.annotation.PostConstruct;
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

//    @PostConstruct
    public void generateSchedule() {

        // ⭐ KHÔNG TẠO LỊCH NẾU ĐÃ CÓ DỮ LIỆU
        if (matchScheduleRepository.count() > 0) {
            System.out.println("✔ Lịch thi đấu đã tồn tại — bỏ qua việc tạo mới.");
            return;
        }

        System.out.println("🔥 Đang tạo lịch thi đấu Premier League...");

        List<Team> teams = teamRepository.findAll();

        if (teams.size() != 10) {
            System.out.println("❌ Không đủ 10 đội — không thể tạo lịch.");
            return;
        }

        // ⭐ Copy + Random thứ tự đội bóng
        List<Team> teamList = new ArrayList<>(teams);
        Collections.shuffle(teamList);

        int totalRounds = teamList.size() - 1;  // 9 vòng mỗi lượt
        int matchesPerRound = teamList.size() / 2;

        LocalDate startDate = nextWeekend(LocalDate.now());
        List<MatchSchedule> allMatches = new ArrayList<>();


        /* ====================== LƯỢT ĐI ======================= */
        for (int round = 0; round < totalRounds; round++) {

            for (int match = 0; match < matchesPerRound; match++) {
                Team home = teamList.get(match);
                Team away = teamList.get(teamList.size() - 1 - match);

                MatchSchedule m = new MatchSchedule();
                m.setHomeTeam(home);
                m.setAwayTeam(away);
                m.setMatchDate(startDate.plusWeeks(round));
                m.setMatchTime(LocalTime.of(16, 0));
                m.setRound(round + 1);
                m.setName(home.getName() + " vs " + away.getName());

                // status mặc định = UPCOMING
                allMatches.add(m);
            }

            // Xoay bảng trừ đội đầu tiên
            teamList.add(1, teamList.remove(teamList.size() - 1));
        }


        /* ====================== LƯỢT VỀ ======================= */

        List<Team> reverseList = new ArrayList<>(teamList);

        // ⭐ Shuffle nhẹ để lượt về đa dạng hơn
        Collections.shuffle(reverseList);

        for (int round = 0; round < totalRounds; round++) {

            for (int match = 0; match < matchesPerRound; match++) {
                Team home = reverseList.get(reverseList.size() - 1 - match);
                Team away = reverseList.get(match);

                MatchSchedule m = new MatchSchedule();
                m.setHomeTeam(home);
                m.setAwayTeam(away);
                m.setMatchDate(startDate.plusWeeks(totalRounds + round));
                m.setMatchTime(LocalTime.of(16, 0));
                m.setRound(totalRounds + round + 1);
                m.setName(home.getName() + " vs " + away.getName());

                allMatches.add(m);
            }

            reverseList.add(1, reverseList.remove(reverseList.size() - 1));
        }

        matchScheduleRepository.saveAll(allMatches);

        System.out.println("🎉 Lịch thi đấu tạo thành công! Tổng số trận: " + allMatches.size());
    }


    /* ====================== HÀM TÌM CUỐI TUẦN GẦN NHẤT ======================= */

    private LocalDate nextWeekend(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();

        if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY)
            return date;

        int daysUntilSaturday = DayOfWeek.SATURDAY.getValue() - dow.getValue();
        return date.plusDays(daysUntilSaturday);
    }
}
