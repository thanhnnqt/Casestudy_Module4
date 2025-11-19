package com.example.premier_league.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "match_stats")
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor @Builder
public class MatchStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long matchId;

    // --- HOME ---
    private int shotsHome;
    private int shotsOnTargetHome;
    private int possessionHome;
    private int passesHome;
    private int accuracyHome;
    private int foulsHome;

    // --- AWAY ---
    private int shotsAway;
    private int shotsOnTargetAway;
    private int possessionAway;
    private int passesAway;
    private int accuracyAway;
    private int foulsAway;
}
