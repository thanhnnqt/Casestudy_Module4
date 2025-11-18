package com.example.premier_league.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "players")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "player_name", length = 100, nullable = false)
    private String name;

    @Column(name = "dob", nullable = false)
    private LocalDate dob;

    @Column(name = "experience", length = 50)
    private String experience;

    @Column(name = "position", length = 50, nullable = false)
    private String position;

    @Column(name = "avatar", length = 500)
    private String avatar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = true)
    private Card card;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = true)
    private Team team;
}
