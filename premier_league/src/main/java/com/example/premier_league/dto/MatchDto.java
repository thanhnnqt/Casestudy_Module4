package com.example.premier_league.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MatchDto {
    public Long id;
    public Long homeTeamId;
    public Long awayTeamId;

    public String homeTeamName;
    public String awayTeamName;

    public Integer homeScore;
    public Integer awayScore;

    public String status;
    public String stadium;

    public String matchDate;
    public String matchTime;
}
