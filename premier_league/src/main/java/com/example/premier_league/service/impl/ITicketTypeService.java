package com.example.premier_league.service.impl;

import com.example.premier_league.entity.TicketType;

import java.util.List;

public interface ITicketTypeService {
    List<TicketType> findAll();
    TicketType findById(Long id);
}
