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

    // Đội nhà
    @ManyToOne
    @JoinColumn(name = "home_team_id", nullable = false)
    private Team homeTeam;

    // Đội khách
    @ManyToOne
    @JoinColumn(name = "away_team_id", nullable = false)
    private Team awayTeam;

    // Tỉ số - mặc định = 0
    private Integer homeScore = 0;
    private Integer awayScore = 0;

    // Thời gian thi đấu
    private LocalDateTime matchDate;

    // Sân vận động
    private String stadium;

    // Trọng tài (optional)
    private String referee;

    // Trạng thái trận đấu
    @Enumerated(EnumType.STRING)
    private MatchStatus status = MatchStatus.SCHEDULED; // mặc định SCHEDULED
}
