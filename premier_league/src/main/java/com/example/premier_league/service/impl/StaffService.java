package com.example.premier_league.service.impl;

import com.example.premier_league.dto.StaffDto;
import com.example.premier_league.entity.Staff;
import com.example.premier_league.entity.Team;
import com.example.premier_league.repository.IStaffRepository;
import com.example.premier_league.repository.ITeamRepository;
import com.example.premier_league.service.IStaffService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StaffService implements IStaffService {

    private final IStaffRepository staffRepository;
    private final ITeamRepository teamRepository;


    public StaffService(IStaffRepository staffRepository,ITeamRepository teamRepository) {
        this.staffRepository = staffRepository;
        this.teamRepository = teamRepository;
    }


    @Override
    public List<Staff> findByTeamId(Long teamId) {
        return staffRepository.findByTeamId(teamId);

    }

    @Override
    public void save(Staff staff) {
        staffRepository.save(staff);
    }


    @Override
    public List<Staff> findAll() {
        return staffRepository.findAll();
    }


    @Override
    public Staff findById(int id) {
        return staffRepository.findById(id).orElse(null);
    }

    @Override
    public List<Staff> findByName(String name) {
        return staffRepository.findByFullNameContainingIgnoreCase(name);
    }

    @Override
    public void update(Staff staff) {
        staffRepository.save(staff);
    }

    @Override
    public void delete(int id) {
        staffRepository.deleteById(id);
    }
}