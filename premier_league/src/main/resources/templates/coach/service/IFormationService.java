package com.example.premier_league.service;

import com.example.premier_league.dto.FormationDto;

import java.util.Map;

public interface IFormationService {
    /**
     * Lấy danh sách tất cả các sơ đồ cố định
     */
    Map<String, FormationDto> getAllFormations();

    /**
     * Lấy 1 sơ đồ theo tên
     */
    FormationDto getFormationByName(String name);
}