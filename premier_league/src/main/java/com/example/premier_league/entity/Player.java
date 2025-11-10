package com.example.premier_league.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @Column(name = "player_name")
    private String name;

    @Column(name = "dob")
    private LocalDate dob;

    @Column(name = "experience", length = 50)
    private String experience;

    @Column(name = "position", length = 50)
    private String position;

    @Column(name = "avatar", length = 500)
    private String avatar;

    @ManyToOne
    @JoinColumn(name = "card_id")
    private Card card;
}
