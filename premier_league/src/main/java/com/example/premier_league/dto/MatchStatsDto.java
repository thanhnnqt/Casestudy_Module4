package com.example.premier_league.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MatchStatsDto {
    // home
    public Integer shotsHome;
    public Integer shotsOnTargetHome;
    public Integer possessionHome;
    public Integer passesHome;
    public Integer accuracyHome;
    public Integer foulsHome;
    public Integer yellowCardsHome;
    public Integer redCardsHome;
    public Integer offsidesHome;
    public Integer cornersHome;

    // away
    public Integer shotsAway;
    public Integer shotsOnTargetAway;
    public Integer possessionAway;
    public Integer passesAway;
    public Integer accuracyAway;
    public Integer foulsAway;
    public Integer yellowCardsAway;
    public Integer redCardsAway;
    public Integer offsidesAway;
    public Integer cornersAway;
}
