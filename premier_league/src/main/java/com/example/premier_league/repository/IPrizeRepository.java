package com.example.premier_league.repository;

import com.example.premier_league.entity.Prize;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IPrizeRepository extends JpaRepository<Prize, Long> {
}
