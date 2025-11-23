package com.example.premier_league.repository;

import com.example.premier_league.entity.Account;
import com.example.premier_league.entity.ClientTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IClientTicketRepository extends JpaRepository<ClientTicket, Long> {
    List<ClientTicket> findByAccountOrderByCreatedAtDesc(Account account);

//    long countByAccount(Account account);

    @Query("SELECT COUNT(ct) FROM ClientTicket ct WHERE ct.account = :account")
    int countByAccount(@Param("account") Account account);

}
