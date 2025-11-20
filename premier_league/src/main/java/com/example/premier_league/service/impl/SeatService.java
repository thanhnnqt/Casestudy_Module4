package com.example.premier_league.service.impl;

import com.example.premier_league.entity.Seat;
import com.example.premier_league.repository.ISeatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SeatService implements ISeatService {
    final ISeatRepository seatRepository;

    public SeatService(ISeatRepository seatRepository) {
        this.seatRepository = seatRepository;
    }

    @Override
    public List<Seat> findAllByRowId(Integer rowId) {
        return seatRepository.findAllByRowId(rowId);
    }

    @Override
    @Transactional
    public void markSeatsOccupiedBySeatNumbers(List<String> seatNumbers) {
        if (seatNumbers == null || seatNumbers.isEmpty()) {
            return;
        }

        List<Seat> seats = seatRepository.findAllBySeatIn(seatNumbers);
        if (seats == null || seats.isEmpty()) {
            return;
        }

        for (Seat seat : seats) {
            seat.setOccupied(true);
        }
        seatRepository.saveAll(seats);
    }

}
