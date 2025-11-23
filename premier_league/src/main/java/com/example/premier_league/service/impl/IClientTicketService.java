package com.example.premier_league.service.impl;

import com.example.premier_league.dto.TicketDto;
import com.example.premier_league.entity.Account;
import com.example.premier_league.entity.ClientTicket;

import java.util.List;

public interface IClientTicketService {
    ClientTicket saveFromTicketDto(TicketDto dto, Account account, String paymentCode);

    List<ClientTicket> findByAccount(Account account);

    long countByAccount(Account account);
    int countTicketsByAccount(Account account);
}
