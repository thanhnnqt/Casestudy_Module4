package com.example.premier_league.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MatchEventDto {
    private Long teamId;
    private Long playerId;
    private Integer minute;
    private String type;
    private String description;
}
