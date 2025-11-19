package com.example.premier_league.repository;

import com.example.premier_league.entity.StadiumRow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IStadiumRowRepository extends JpaRepository<StadiumRow, Integer> {
    List<StadiumRow> findAllBySessionId(Integer sessionId);
}
