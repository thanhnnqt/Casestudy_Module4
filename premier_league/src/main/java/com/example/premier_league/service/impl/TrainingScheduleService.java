package com.example.premier_league.service.impl;

import com.example.premier_league.entity.TrainingSchedule;
import com.example.premier_league.repository.ITrainingScheduleRepository;
import com.example.premier_league.service.ITrainingScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainingScheduleService implements ITrainingScheduleService {

    private final ITrainingScheduleRepository iTrainingScheduleRepository;

    @Override
    public List<TrainingSchedule> findByTeamId(Long teamId) {
        return iTrainingScheduleRepository.findByTeamIdOrderByStartTimeDesc(teamId);
    }

    @Override
    public TrainingSchedule findById(Long trainingId) {
        return iTrainingScheduleRepository.findById(trainingId).orElse(null);
    }

    @Override
    public void save(TrainingSchedule trainingSchedule) {
        iTrainingScheduleRepository.save(trainingSchedule);
    }

    @Override
    public void deleteById(Long trainingId) {
        iTrainingScheduleRepository.deleteById(trainingId);
    }
}