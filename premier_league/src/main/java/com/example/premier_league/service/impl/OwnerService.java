package com.example.premier_league.service.impl;

import com.example.premier_league.dto.OwnerDto;
import com.example.premier_league.entity.Account;
import com.example.premier_league.entity.Owner;
import com.example.premier_league.entity.Team;
import com.example.premier_league.repository.IAccountRepository;
import com.example.premier_league.repository.IOwnerRepository;
import com.example.premier_league.repository.ITeamRepository;
import com.example.premier_league.service.IOwnerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OwnerService implements IOwnerService {

    private final IOwnerRepository ownerRepository;
    private final IAccountRepository accountRepository;
    private final ITeamRepository teamRepository;

    @Override
    public List<Owner> findAll() {
        return ownerRepository.findAll();
    }

    @Override
    public Owner findById(Long id) {
        return ownerRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public void saveFromDto(OwnerDto ownerDto) {
        Owner owner;
        // Kiểm tra nếu là Update
        if (ownerDto.getId() != null) {
            owner = ownerRepository.findById(ownerDto.getId()).orElse(new Owner());
        } else {
            owner = new Owner();
        }

        // Copy thông tin cơ bản
        owner.setName(ownerDto.getName());
        owner.setDob(ownerDto.getDob());
        owner.setPhoneNumber(ownerDto.getPhoneNumber());

        // Tìm và gán Account
        if (ownerDto.getAccountId() != null) {
            Account account = accountRepository.findById(ownerDto.getAccountId())
                    .orElseThrow(() -> new RuntimeException("Account not found"));
            owner.setAccount(account);
        }

        // Tìm và gán Team
        if (ownerDto.getTeamId() != null) {
            Team team = teamRepository.findById(ownerDto.getTeamId())
                    .orElseThrow(() -> new RuntimeException("Team not found"));
            owner.setTeam(team);
        }

        ownerRepository.save(owner);
    }

    @Override
    public void delete(Long id) {
        ownerRepository.deleteById(id);
    }
}