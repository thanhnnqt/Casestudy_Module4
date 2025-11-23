package com.example.premier_league.service.impl;

import com.example.premier_league.dto.TicketDto;
import com.example.premier_league.entity.Account;
import com.example.premier_league.entity.ClientTicket;
import com.example.premier_league.repository.IClientTicketRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ClientTicketService implements IClientTicketService {
    private final IClientTicketRepository clientTicketRepository;

    public ClientTicketService(IClientTicketRepository clientTicketRepository) {
        this.clientTicketRepository = clientTicketRepository;
    }

    @Override
    public ClientTicket saveFromTicketDto(TicketDto dto, Account account, String paymentCode) {
        ClientTicket ct = new ClientTicket();
        ct.setAccount(account);

        ct.setStadium(dto.getStadium());
        ct.setHomeTeam(dto.getHomeTeam());
        ct.setAwayTeam(dto.getAwayTeam());
        ct.setAddress(dto.getAddress());
        ct.setDateMatch(dto.getDateMatch());
        ct.setTimeMatch(dto.getTimeMatch());
        ct.setQuantity(dto.getQuantity());
        ct.setStandSession(dto.getStandSession());
        ct.setSeatNumber(dto.getSeatNumber());
        ct.setTotalPay(dto.getTotalPay());

        ct.setPaymentCode(paymentCode);
        ct.setStatus("PAID");
        ct.setCreatedAt(LocalDateTime.now());

        return clientTicketRepository.save(ct);
    }

    @Override
    public List<ClientTicket> findByAccount(Account account) {
        return clientTicketRepository.findByAccountOrderByCreatedAtDesc(account);
    }

    @Override
    public long countByAccount(Account account) {
        return clientTicketRepository.countByAccount(account);
    }

    @Override
    public int countTicketsByAccount(Account account) {
        return clientTicketRepository.countByAccount(account);
    }

}
