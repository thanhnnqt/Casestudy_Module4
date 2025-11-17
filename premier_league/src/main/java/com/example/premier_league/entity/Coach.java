package com.example.premier_league.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "coach")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Coach {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    private String nationality;

    private String phoneNumber;

    private String email;

    private String avatarUrl;

    private int experienceYears;

    private String specialization; //Chuyên môn (HLV chiến thuật, thể lực, thủ môn..)

    private String licenseLevel; //Bằng cấp

    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;
}
