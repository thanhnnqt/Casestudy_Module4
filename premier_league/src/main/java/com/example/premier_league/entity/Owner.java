package com.example.premier_league.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Owner {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    private LocalDate dob;
    private String phoneNumber;
    @OneToOne
    @JoinColumn(name = "account_id")
    private Account account;
    @OneToOne
    @JoinColumn(name = "team_id")
    private Team team;
}
