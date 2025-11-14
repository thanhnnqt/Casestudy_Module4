package com.example.premier_league.util;

import com.example.premier_league.entity.MatchSchedule;
import com.example.premier_league.entity.MatchStatus;
import com.example.premier_league.entity.Team;
import com.example.premier_league.repository.IMatchScheduleRepository;
import com.example.premier_league.repository.ITeamRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class MatchScheduleGenerator {

    private final ITeamRepository teamRepository;

    private final IMatchScheduleRepository matchScheduleRepository;

    public MatchScheduleGenerator (IMatchScheduleRepository matchScheduleRepository, ITeamRepository teamRepository){
        this.matchScheduleRepository = matchScheduleRepository;
        this.teamRepository = teamRepository;
    }

    @PostConstruct
    public void generateSchedule() {
        List<Team> teams = teamRepository.findAll();
        if (teams.size() != 10) return; // chỉ áp dụng 10 đội

        // Xóa lịch cũ nếu muốn chạy lại
        matchScheduleRepository.deleteAll();

        List<Team> teamList = new ArrayList<>(teams);

        int totalRounds = teamList.size() - 1; // 9 vòng/lượt
        int matchesPerRound = teamList.size() / 2;
        LocalDate startDate = nextWeekend(LocalDate.now());

        List<MatchSchedule> allMatches = new ArrayList<>();

        // Lượt đi
        for (int round = 0; round < totalRounds; round++) {
            for (int match = 0; match < matchesPerRound; match++) {
                Team home = teamList.get(match);
                Team away = teamList.get(teamList.size() - 1 - match);

                MatchSchedule m = new MatchSchedule();
                m.setHomeTeam(home);
                m.setAwayTeam(away);
                m.setMatchDate(startDate.plusWeeks(round));
                m.setMatchTime(LocalTime.of(16,0));
                m.setRound(round + 1);
                m.setStatus(MatchStatus.SCHEDULED);
                m.setName(home.getName() + " vs " + away.getName());
                allMatches.add(m);
            }
            // Xoay vòng trừ đội đầu
            teamList.add(1, teamList.remove(teamList.size() - 1));
        }

        // Lượt về (đổi home/away)
        List<Team> reverseTeams = new ArrayList<>(teams);
        for (int round = 0; round < totalRounds; round++) {
            for (int match = 0; match < matchesPerRound; match++) {
                Team home = reverseTeams.get(teamList.size() - 1 - match);
                Team away = reverseTeams.get(match);

                MatchSchedule m = new MatchSchedule();
                m.setHomeTeam(home);
                m.setAwayTeam(away);
                m.setMatchDate(startDate.plusWeeks(totalRounds + round));
                m.setMatchTime(LocalTime.of(16,0));
                m.setRound(totalRounds + round + 1);
                m.setStatus(MatchStatus.SCHEDULED);
                m.setName(home.getName() + " vs " + away.getName());
                allMatches.add(m);
            }
            reverseTeams.add(1, reverseTeams.remove(reverseTeams.size() - 1));
        }

        matchScheduleRepository.saveAll(allMatches);
        System.out.println("Lịch thi đấu đã được tạo!");
    }

    // Tìm ngày thứ 7 gần nhất hoặc chủ nhật
    private LocalDate nextWeekend(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        if (dow == DayOfWeek.SATURDAY) return date;
        if (dow == DayOfWeek.SUNDAY) return date;
        int daysUntilSaturday = DayOfWeek.SATURDAY.getValue() - dow.getValue();
        return date.plusDays(daysUntilSaturday);
    }
}
