package com.example.premier_league.service;

import com.example.premier_league.entity.TrainingSchedule;

import java.util.List;

public interface ITrainingScheduleService {
    List<TrainingSchedule> findByTeamId(Long teamId);
    TrainingSchedule findById(Long trainingId);
    void save(TrainingSchedule trainingSchedule);
    void deleteById(Long trainingId);
}