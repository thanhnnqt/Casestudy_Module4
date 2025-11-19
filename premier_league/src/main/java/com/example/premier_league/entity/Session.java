package com.example.premier_league.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Session {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "name")
    private String name;
    private Integer capacity;
    @ManyToOne
    @JoinColumn(name = "stadium_id")
    private Stadium stadium;
    @OneToMany
    @JoinColumn(name = "session_id")
    List<StadiumRow> rowList;
}
