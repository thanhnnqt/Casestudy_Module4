package com.example.premier_league.service;

import com.example.premier_league.dto.OwnerDto;
import com.example.premier_league.entity.Owner;
import java.util.List;

public interface IOwnerService {
    List<Owner> findAll();
    Owner findById(Long id);
    void saveFromDto(OwnerDto ownerDto);
    void delete(Long id);
}