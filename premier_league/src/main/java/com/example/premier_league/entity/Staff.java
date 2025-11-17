package com.example.premier_league.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "staffs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Staff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String fullName;

    private LocalDate dateOfBirth;

    private String gender;

    private String nationality; // Quốc tịch

    private String position; // Vị trí công tác

    private String role; // Vai trò cụ thể trong đội

    private LocalDate joinDate; // Ngày gia nhập đội

    private String phoneNumber;

    private String email;

    private String status;

    private String avatarUrl; // Ảnh đại diện

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;
}
