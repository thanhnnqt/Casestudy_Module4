package com.example.premier_league.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer price;
    @ManyToOne
    @JoinColumn(name = "audience_id")
    Audience audience;
    @ManyToOne
    @JoinColumn(name = "ticketType_id")
    TicketType ticketType;
    @ManyToOne
    @JoinColumn(name = "matchSchedule_id")
    private MatchSchedule matchSchedule;
}
