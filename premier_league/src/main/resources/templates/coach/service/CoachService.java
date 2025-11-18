package com.example.premier_league.service;

import com.example.premier_league.entity.Coach;
import com.example.premier_league.repository.ICoachRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CoachService implements ICoachService {

    private final ICoachRepository coachRepository;

    public CoachService(ICoachRepository coachRepository) {
        this.coachRepository = coachRepository;
    }

    @Override
    public List<Coach> findAll() {
        return coachRepository.findAll();
    }

    @Override
    public void save(Coach coach) {
        coachRepository.save(coach);
    }

    @Override
    public Coach findById(int id) {
        return coachRepository.findById(id).orElse(null);
    }

    @Override
    public List<Coach> findByName(String name) {
        return coachRepository.findByFullNameContainingIgnoreCase(name);
    }

    @Override
    public void update(Coach coach) {
        if (coachRepository.existsById(coach.getId())) {
            coachRepository.save(coach);
        }
    }

    @Override
    public void delete(int id) {
        if (coachRepository.existsById(id)) {
            coachRepository.deleteById(id);
        }
    }
}
