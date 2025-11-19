package com.example.premier_league.service.impl;

import com.example.premier_league.entity.Ticket;
import com.example.premier_league.repository.ITicketRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TicketService implements ITicketService {
    final ITicketRepository ticketRepository;

    public TicketService(ITicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Override
    public boolean create(Ticket ticket) {
        return ticketRepository.save(ticket) != null;
    }

    @Override
    public Page<Ticket> findAll(Pageable pageable) {
        return ticketRepository.findAll(pageable);
    }

    @Override
    public List<Ticket>
    findAllList() {
        return ticketRepository.findAll();
    }

    @Override
    public Ticket findByHomeTeamAndAwayTeam(String homeTeam, String awayTeam) {
        return ticketRepository.findByHomeTeamAndAwayTeam(homeTeam, awayTeam);
    }
}
