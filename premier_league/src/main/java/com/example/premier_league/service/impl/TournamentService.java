package com.example.premier_league.service.impl;

import com.example.premier_league.entity.Tournament;
import com.example.premier_league.repository.ITournamentRepository;
import com.example.premier_league.service.ITournamentService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TournamentService implements ITournamentService {

    private final ITournamentRepository tournamentRepository;
    public TournamentService(ITournamentRepository tournamentRepository){
        this.tournamentRepository = tournamentRepository;
    }

    @Override
    public List<Tournament> findAll() {
        return tournamentRepository.findAll();
    }

    @Override
    public Tournament findById(Long id) {
        return tournamentRepository.findById(id).orElse(null);
    }

    @Override
    public Tournament save(Tournament tournament) {
        return tournamentRepository.save(tournament);
    }

    @Override
    public void delete(Long id) {
        tournamentRepository.deleteById(id);
    }
}
