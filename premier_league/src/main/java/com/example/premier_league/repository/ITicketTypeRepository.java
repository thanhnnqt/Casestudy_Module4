package com.example.premier_league.repository;

import com.example.premier_league.entity.TicketType;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ITicketTypeRepository extends JpaRepository<TicketType, Long> {
}
