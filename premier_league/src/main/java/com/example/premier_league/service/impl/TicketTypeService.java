package com.example.premier_league.service.impl;


import com.example.premier_league.entity.TicketType;
import com.example.premier_league.repository.ITicketTypeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TicketTypeService implements ITicketTypeService {
    final ITicketTypeRepository ticketTypeRepository;

    public TicketTypeService(ITicketTypeRepository ticketTypeRepository) {
        this.ticketTypeRepository = ticketTypeRepository;
    }

    @Override
    public List<TicketType> findAll() {
        return ticketTypeRepository.findAll();
    }

    @Override
    public TicketType findById(Long id) {
        return ticketTypeRepository.findById(id).orElse(null);
    }
}
