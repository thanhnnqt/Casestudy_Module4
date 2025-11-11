package com.example.premier_league.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "coach")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coach {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 🔹 Mã định danh duy nhất cho huấn luyện viên

    @Column(nullable = false)
    private String fullName; // 🔹 Họ và tên của huấn luyện viên

    private String nationality; // 🔹 Quốc tịch

    private String phoneNumber; // 🔹 Số điện thoại liên hệ

    private String email; // 🔹 Email (có thể dùng để đăng nhập hoặc nhận thông báo)

    private String avatarUrl; // 🔹 Ảnh đại diện (lưu link trên Cloudinary)

    private int experienceYears; // 🔹 Số năm kinh nghiệm huấn luyện

    private String specialization; // 🔹 Chuyên môn (vd: Chiến thuật tấn công, phòng ngự…)

    private String licenseLevel; // 🔹 Trình độ huấn luyện (vd: AFC Pro, UEFA A…)

    private String note; // 🔹 Ghi chú thêm (vd: tình trạng hợp đồng, sức khỏe…)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team; // 🔹 Mối quan hệ nhiều-huấn luyện viên-thuộc-về-1 đội bóng

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account; // 🔹 Liên kết với tài khoản đăng nhập (phân quyền là “COACH”)
}
