package com.example.premier_league.service.impl;

import com.example.premier_league.entity.Stadium;
import com.example.premier_league.repository.IStadiumRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StadiumService implements IStadiumService {
    final IStadiumRepository stadiumRepository;

    public StadiumService(IStadiumRepository stadiumRepository) {
        this.stadiumRepository = stadiumRepository;
    }

    @Override
    public List<Stadium> findAll() {
        return stadiumRepository.findAll();
    }

    @Override
    public Stadium findByName(String name) {
        return stadiumRepository.findByName(name);
    }
}
