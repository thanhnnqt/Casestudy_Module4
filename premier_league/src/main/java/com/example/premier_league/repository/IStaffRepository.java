package com.example.premier_league.repository;

import com.example.premier_league.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IStaffRepository extends JpaRepository<Staff, Integer> {
    List<Staff> findByFullNameContainingIgnoreCase(String name);

    List<Staff> findByTeamId(Long teamId);
}
