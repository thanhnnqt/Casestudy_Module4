package com.example.premier_league.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "teams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    private String shortName; // Tên viết tắt

    private String country; // Quốc gia

    private String city; // Thành phố

    private String stadium; // Sân vận động chính của đội

    private String coachName; // Tên huấn luyện viên

    private String logoUrl; // Link logo đội

    private String description; // Mô tả ngắn gọn về đội

    private int totalPlayers; // Tổng số cầu thủ hiện tại trong đội

    private int winCount; // Tổng số trận thắng
    private int drawCount; // Tổng số trận hòa
    private int loseCount; // Tổng số trận thua
    private int points; // Tổng điểm
}

