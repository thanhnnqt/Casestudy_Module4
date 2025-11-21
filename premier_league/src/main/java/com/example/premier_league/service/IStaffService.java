package com.example.premier_league.service;

import com.example.premier_league.dto.StaffDto;
import com.example.premier_league.entity.Staff;

import java.util.List;

public interface IStaffService {
    List<Staff> findAll();

    List<Staff> findByTeamId(Long teamId);

    void save (Staff staff);

    Staff findById(int id);

    List<Staff> findByName(String name);

    void update(Staff staff);

    void delete(int id);

}
