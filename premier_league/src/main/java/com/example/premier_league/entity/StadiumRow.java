package com.example.premier_league.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "stadium_row")
public class StadiumRow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String stadiumRow;
    @Column(name = "session_id")
    private Integer sessionId;
    @OneToMany
    @JoinColumn(name = "row_id")
    List<Seat> seatList;
}
