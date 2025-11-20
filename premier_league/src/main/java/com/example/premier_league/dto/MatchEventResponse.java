package com.example.premier_league.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchEventResponse {
    private Long id;
    private int minute;
    private String type;
    private String description;
    private String teamName;
    private String playerName;
}

