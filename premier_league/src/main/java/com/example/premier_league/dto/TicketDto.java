package com.example.premier_league.dto;

import com.example.premier_league.entity.TicketType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TicketDto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String stadium;
    private String homeTeam;
    private String awayTeam;
    private String address;
    private Integer quantity;
    private LocalDate dateMatch;
    private LocalTime timeMatch;
    private String seatNumber;
    private String standSession;
    @ManyToOne
    @JoinColumn(name = "price")
    private TicketType ticketType;
}
