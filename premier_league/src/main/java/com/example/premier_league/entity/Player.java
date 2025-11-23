package com.example.premier_league.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.Period;

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

    private int yellowCards = 0;

    private int redCards = 0;

    private Integer seasonYellowCards = 0;     // thẻ vàng tích lũy
    private Integer suspensionMatchesRemaining; // số trận còn bị treo giò

    // Hàm tính tuổi (Sửa lỗi Property 'age' not found)
    public int getAge() {
        if (this.dob == null) return 0;
        return Period.between(this.dob, LocalDate.now()).getYears();
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = true)
    private Card card;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = true)
    private Team team;


}
