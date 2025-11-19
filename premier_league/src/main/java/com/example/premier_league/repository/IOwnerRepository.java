package com.example.premier_league.repository;

import com.example.premier_league.entity.Owner;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IOwnerRepository extends JpaRepository<Owner, Long> {
}
