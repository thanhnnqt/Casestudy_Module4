package com.example.premier_league.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MatchStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long matchId;

    private Integer shotsHome = 0;
    private Integer shotsAway = 0;

    private Integer shotsOnTargetHome = 0;
    private Integer shotsOnTargetAway = 0;

    private Integer possessionHome = 0;
    private Integer possessionAway = 0;

    private Integer passesHome = 0;
    private Integer passesAway = 0;

    private Integer accuracyHome = 0;
    private Integer accuracyAway = 0;

    private Integer foulsHome = 0;
    private Integer foulsAway = 0;

    private Integer yellowCardsHome = 0;
    private Integer yellowCardsAway = 0;

    private Integer redCardsHome = 0;
    private Integer redCardsAway = 0;

    private Integer offsidesHome = 0;
    private Integer offsidesAway = 0;

    private Integer cornersHome = 0;
    private Integer cornersAway = 0;

    // 👉 Constructor cần thiết để fix lỗi
    public MatchStats(Long matchId) {
        this.matchId = matchId;
    }
}
