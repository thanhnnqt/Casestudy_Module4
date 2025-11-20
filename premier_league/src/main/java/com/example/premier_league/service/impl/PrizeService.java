package com.example.premier_league.service.impl;

import com.example.premier_league.entity.Prize;
import com.example.premier_league.repository.IPrizeRepository;
import com.example.premier_league.service.IPrizeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class PrizeService implements IPrizeService {

    @Autowired
    private final IPrizeRepository prizeRepository;

    public PrizeService(IPrizeRepository prizeRepository) {
        this.prizeRepository = prizeRepository;
    }

    @Override
    public List<Prize> findAll() {
        return prizeRepository.findAll();
    }

    @Override
    public Prize findById(Long id) {
        return prizeRepository.findById(id).orElse(null);
    }

    @Override
    public Prize save(Prize prize) {
        return prizeRepository.save(prize);
    }

    @Override
    public void delete(Long id) {
        prizeRepository.deleteById(id);
    }

}
