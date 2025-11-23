package com.example.premier_league.service.impl;

import com.example.premier_league.entity.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ITicketService {
    boolean save(Ticket ticket);

    Page<Ticket> findAll(Pageable pageable);

    List<Ticket> findAllList();

    Ticket findByHomeTeamAndAwayTeam(String homeTeam, String awayTeam);

    List<Ticket> findAllByHomeTeam(String homeTeam);

    Ticket findById(Integer id);

    void delete(Ticket ticket);
}
