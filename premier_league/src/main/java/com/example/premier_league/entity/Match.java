package com.example.premier_league.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "matches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "home_team_id", nullable = false)
    private Team homeTeam;

    @ManyToOne
    @JoinColumn(name = "away_team_id", nullable = false)
    private Team awayTeam;

    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer homeScore = 0;

    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer awayScore = 0;


    private LocalDateTime matchDate;

    private String stadium;

    private String referee;

    @Enumerated(EnumType.STRING)
    private MatchStatus status = MatchStatus.SCHEDULED;

    @OneToOne(mappedBy = "match")
    private MatchSchedule schedule;
}
