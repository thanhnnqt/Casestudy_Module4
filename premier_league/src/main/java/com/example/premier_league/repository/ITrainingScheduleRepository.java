package com.example.premier_league.repository;

import com.example.premier_league.entity.TrainingSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ITrainingScheduleRepository extends JpaRepository<TrainingSchedule, Long> {
    /**
     * Lấy danh sách lịch tập của một đội, sắp xếp theo thời gian mới nhất
     */
    List<TrainingSchedule> findByTeamIdOrderByStartTimeDesc(Long teamId);
}