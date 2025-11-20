package com.example.premier_league.service.impl;

import com.example.premier_league.entity.Seat;

import java.util.List;

public interface ISeatService {
    List<Seat> findAllByRowId(Integer rowId);


    void markSeatsOccupiedBySeatNumbers(List<String> seatNumbers);
}
