package com.example.premier_league.service.impl;

import com.example.premier_league.entity.Stadium;

import java.util.List;

public interface IStadiumService {
    List<Stadium> findAll();
    Stadium findByName(String name);

}
