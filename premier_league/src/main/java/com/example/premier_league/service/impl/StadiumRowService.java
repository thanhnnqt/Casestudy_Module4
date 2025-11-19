package com.example.premier_league.service.impl;

import com.example.premier_league.entity.StadiumRow;
import com.example.premier_league.repository.IStadiumRowRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StadiumRowService implements IStadiumRowService {
    final IStadiumRowRepository stadiumRowRepository;

    public StadiumRowService(IStadiumRowRepository stadiumRowRepository) {
        this.stadiumRowRepository = stadiumRowRepository;
    }

    @Override
    public List<StadiumRow> findAllBySessionId(Integer sessionId) {
        return stadiumRowRepository.findAllBySessionId(sessionId);
    }
}
