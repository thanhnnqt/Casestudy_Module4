package com.example.premier_league.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RankingsDto {
    public Long id;
    public String name;
    public String logoUrl;

    public int points;
    public int winCount;
    public int drawCount;
    public int loseCount;

    public int goalsFor;
    public int goalsAgainst;
    public int goalDifference;
}
