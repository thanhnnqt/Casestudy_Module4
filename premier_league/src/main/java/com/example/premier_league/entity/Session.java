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
    private String name;        // Ví dụ: A, B, C, D
    @Column(name = "capacity")
    private Integer capacity;   // Ví dụ: 100

    @Column(name = "last_assigned_seat")
    private Integer lastAssignedSeat = 0; // đã cấp đến ghế số mấy

    @ManyToOne
    @JoinColumn(name = "stadium_id")
    private Stadium stadium;

}
