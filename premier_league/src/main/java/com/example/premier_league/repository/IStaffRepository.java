package com.example.premier_league.repository;

import com.example.premier_league.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IStaffRepository extends JpaRepository<Staff, Integer> {
}
