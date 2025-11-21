package com.example.premier_league.repository;

import com.example.premier_league.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface ITicketRepository extends JpaRepository<Ticket, Integer> {
    Ticket findByHomeTeamAndAwayTeam(String homeTeam, String awayTeam);

    List<Ticket> findAllByHomeTeam(String homeTeam);
}
