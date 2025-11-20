package com.example.premier_league.repository;

import com.example.premier_league.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ISessionRepository extends JpaRepository<Session, Integer> {
    List<Session> findAllByStadium_Id(Integer stadiumId);
    Session findByNameAndStadium_Name(String name, String stadiumName);
}
