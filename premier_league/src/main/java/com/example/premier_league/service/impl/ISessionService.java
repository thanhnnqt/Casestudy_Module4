package com.example.premier_league.service.impl;

import com.example.premier_league.entity.Session;

import java.util.List;

public interface ISessionService {
    List<Session> findAll();
    List<Session> findAllByStadium_Id(Integer stadiumId);
}
