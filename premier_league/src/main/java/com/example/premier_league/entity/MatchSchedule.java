package com.example.premier_league.entity;


import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "matches")
public class MatchSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private LocalDate matchDate;

    private LocalTime matchTime;

//    @ManyToOne
//    @JoinColumn(name = "home_team_id")
//    private Team homeTeam;

//    @ManyToOne
//    @JoinColumn(name = "away_team_id")
//    private Team awayTeam;

    private Integer round;

    private String note;
}
