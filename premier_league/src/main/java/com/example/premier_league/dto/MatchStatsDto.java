package com.example.premier_league.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MatchStatsDto {

    private int shotsHome;
    private int shotsAway;

    private int shotsOnTargetHome;
    private int shotsOnTargetAway;

    private int possessionHome;
    private int possessionAway;

    private int passesHome;
    private int passesAway;

    private int accuracyHome;
    private int accuracyAway;

    private int foulsHome;
    private int foulsAway;
}
