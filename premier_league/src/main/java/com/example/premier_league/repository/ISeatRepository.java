package com.example.premier_league.repository;

import com.example.premier_league.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ISeatRepository extends JpaRepository<Seat, Integer> {
    List<Seat> findAllByRowId(Integer rowId);
    List<Seat> findAllBySeatIn(List<String> seatNumbers);
}
