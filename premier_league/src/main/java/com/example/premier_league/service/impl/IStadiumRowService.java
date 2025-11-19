package com.example.premier_league.service.impl;

import com.example.premier_league.entity.StadiumRow;

import java.util.List;

public interface IStadiumRowService {
    List<StadiumRow> findAllBySessionId(Integer sessionId);
}
