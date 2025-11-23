package com.example.premier_league.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "client_ticket")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClientTicket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Người mua (Account)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    // Các thông tin lấy từ TicketDto
    private String stadium;
    private String homeTeam;
    private String awayTeam;
    private String address;

    private LocalDate dateMatch;
    private LocalTime timeMatch;

    private Integer quantity;
    private String standSession;   // Khu
    @Column(length = 2000)
    private String seatNumber;     // Chuỗi A1,A2,...

    private Integer totalPay;         // Tổng tiền

    // Thông tin thanh toán
    private String paymentCode;    // vnp_TxnRef
    private String status;         // PAID / CANCEL...

    private LocalDateTime createdAt;
}
