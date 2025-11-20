package com.example.premier_league.service.impl;

import com.example.premier_league.entity.Staff;
import com.example.premier_league.repository.IStaffRepository;
import com.example.premier_league.service.IStaffService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StaffService implements IStaffService {

    private final IStaffRepository staffRepository;

    public StaffService(IStaffRepository staffRepository) {
        this.staffRepository = staffRepository;
    }

    @Override
    public List<Staff> findAll() {
        return staffRepository.findAll();
    }

    @Override
    public void save(Staff staff) {
        staffRepository.save(staff);
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