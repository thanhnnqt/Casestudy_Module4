package com.example.premier_league.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "coaches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Coach {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Họ tên HLV
    @Column(nullable = false, length = 100)
    private String fullName;

    // Chức vụ: Head Coach, Assistant Coach,...
    @Column(nullable = false, length = 50)
    private String role;

    // Link ảnh avatar
    @Column(length = 255)
    private String avatarUrl;

    // Quốc tịch
    @Column(length = 50)
    private String nationality;

    private LocalDate dateOfBirth;

    // Số năm kinh nghiệm
    private Integer experienceYears;

    // Chứng chỉ huấn luyện: UEFA Pro/A/B...
    @Column(length = 50)
    private String licenseLevel;

    private LocalDate joinDate;

    // Trạng thái: Active / Inactive
    @Column(length = 20)
    private String status = "Active";

    @Column(length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(columnDefinition = "TEXT")
    private String note;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

}
