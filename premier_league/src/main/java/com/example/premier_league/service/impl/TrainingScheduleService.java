package com.example.premier_league.service.impl;

import com.example.premier_league.entity.MatchSchedule;
import com.example.premier_league.entity.TrainingSchedule;
import com.example.premier_league.repository.IMatchScheduleRepository;
import com.example.premier_league.repository.ITrainingScheduleRepository;
import com.example.premier_league.service.ITrainingScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainingScheduleService implements ITrainingScheduleService {

    private final ITrainingScheduleRepository iTrainingScheduleRepository;
    private final IMatchScheduleRepository iMatchScheduleRepository;

    @Override
    public List<TrainingSchedule> findByTeamId(Long teamId) {
        return iTrainingScheduleRepository.findByTeamIdOrderByStartTimeDesc(teamId);
    }

    @Override
    public TrainingSchedule findById(Long trainingId) {
        return iTrainingScheduleRepository.findById(trainingId).orElse(null);
    }

    @Override
    public void save(TrainingSchedule ts) {
        // === START VALIDATION ===
        LocalDateTime now = LocalDateTime.now();

        // 1. Phải ở tương lai
        if (ts.getStartTime().isBefore(now)) {
            throw new RuntimeException("Thời gian tập luyện phải lớn hơn thời gian hiện tại!");
        }

        // 2. Độ dài >= 30 phút
        long duration = Duration.between(ts.getStartTime(), ts.getEndTime()).toMinutes();
        if (duration < 30) {
            throw new RuntimeException("Buổi tập phải kéo dài ít nhất 30 phút (Kết thúc > Bắt đầu 30p)!");
        }

        // 3. Cách trận đấu sắp tới 6 tiếng
        List<MatchSchedule> matches = iMatchScheduleRepository
                .findAllByHomeTeamIdOrAwayTeamIdOrderByMatchDateAscMatchTimeAsc(ts.getTeam().getId(), ts.getTeam().getId());

        for (MatchSchedule match : matches) {
            LocalDateTime matchTime = LocalDateTime.of(match.getMatchDate(), match.getMatchTime());

            // Chỉ check những trận chưa diễn ra và diễn ra sau giờ tập
            if (matchTime.isAfter(ts.getStartTime())) {
                long hoursDiff = Duration.between(ts.getEndTime(), matchTime).toHours();
                if (hoursDiff < 6) {
                    throw new RuntimeException("Lịch tập vi phạm: Phải kết thúc trước trận đấu ("
                            + match.getName() + ") ít nhất 6 tiếng!");
                }
                break; // Check trận gần nhất là đủ
            }
        }
        // === END VALIDATION ===

        iTrainingScheduleRepository.save(ts);
    }

    @Override
    public void deleteById(Long trainingId) {
        iTrainingScheduleRepository.deleteById(trainingId);
    }
}