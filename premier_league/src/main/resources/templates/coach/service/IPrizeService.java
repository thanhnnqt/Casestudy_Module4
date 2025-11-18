package com.example.premier_league.service;

import com.example.premier_league.entity.Prize;

import java.util.List;

public interface IPrizeService {
    List<Prize> findAll();
    Prize findById(Long id);
    Prize save(Prize prize);
    void delete(Long id);
}
