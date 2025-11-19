package com.example.premier_league.repository;

import com.example.premier_league.entity.Stadium;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IStadiumRepository extends JpaRepository<Stadium, Long> {
    Stadium findByName(String name);
}
