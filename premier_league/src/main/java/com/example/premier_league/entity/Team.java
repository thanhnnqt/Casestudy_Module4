package com.example.premier_league.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "teams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String shortName; // Tên viết tắt

    private String country; // Quốc gia

    private String city; // Thành phố

    private String stadium; // Sân vận động chính

    private String coachName; // Huấn luyện viên

    private String logoUrl; // Logo đội

    @Column(length = 1000)
    private String description;

    private int totalPlayers;

    // Thống kê mùa giải
    private int winCount;
    private int drawCount;
    private int loseCount;

    private int goalsFor;       // bàn thắng
    private int goalsAgainst;   // bàn thua
    private int goalDifference; // hiệu số (goalsFor - goalsAgainst)

    private int points; // điểm số (3 thắng - 1 hòa - 0 thua)

    // =================================================================
    // SỬA ĐỔI QUAN TRỌNG Ở ĐÂY:
    // =================================================================
    // Vì Account là @ManyToOne (Nhiều Acc thuộc 1 Team)
    // Nên Team phải là @OneToMany (1 Team có danh sách các Acc)
    @OneToMany(mappedBy = "team")
    @JsonIgnore
    private List<Account> accounts = new ArrayList<>();

    @JsonIgnore
    @ManyToMany(mappedBy = "teams")
    private Set<Tournament> tournaments = new HashSet<>();

    @OneToMany(mappedBy = "team")
    @JsonManagedReference
    private List<Coach> coaches;
}