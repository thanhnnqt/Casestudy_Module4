package com.example.premier_league.service;

import com.example.premier_league.entity.Tournament;

import java.util.List;

public interface ITournamentService {
    List<Tournament> findAll();
    Tournament findById(Long id);
    Tournament save(Tournament t);
    void delete(Long id);

    void updateTeamsForTournament(Long tournamentId, List<Long> teamIds);
    boolean isTournamentStarted(Long tournamentId);
    void copyTeamsFromTournament(Long targetTournamentId, Long sourceTournamentId);
    boolean existsBySeason(String season);
}
