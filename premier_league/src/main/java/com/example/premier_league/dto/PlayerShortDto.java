package com.example.premier_league.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlayerShortDto {
    private Long id;
    private String name;
    private String position;
    private String avatar;

    private int seasonYellowCards;
    private int suspensionMatchesRemaining;
}

