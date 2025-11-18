package com.example.premier_league.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * sơ đồ chiến thuật
 */
@Getter
@Setter
public class FormationDto {


    private String name;


    private Map<String, String> positions;

    public FormationDto(String name, Map<String, String> positions) {
        this.name = name;
        this.positions = positions;
    }
}