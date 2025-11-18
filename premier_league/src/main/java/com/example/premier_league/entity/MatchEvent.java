package com.example.premier_league.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "match_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MatchEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long matchId;

    private Long teamId; // nullable

    private Integer minute; // phút xảy ra

    private String type; // GOAL, YELLOW_CARD, RED_CARD, PENALTY, MATCH_END, ...

    @Column(length = 1000)
    private String description;

    private Instant createdAt = Instant.now();
}
