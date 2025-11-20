package com.example.premier_league.service.impl;

import com.example.premier_league.entity.Team;
import com.example.premier_league.entity.Tournament;
import com.example.premier_league.repository.ITeamRepository;
import com.example.premier_league.repository.ITournamentRepository;
import com.example.premier_league.service.ITournamentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
public class TournamentService implements ITournamentService {

    private final ITournamentRepository tournamentRepository;
    private final ITeamRepository teamRepository; // <-- THÊM DEPENDENCY
    public TournamentService(ITournamentRepository tournamentRepository, ITeamRepository teamRepository){
        this.tournamentRepository = tournamentRepository;
        this.teamRepository = teamRepository;
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

    @Override
    @Transactional
    public void updateTeamsForTournament(Long tournamentId, List<Long> teamIds) {
        Tournament tournament = tournamentRepository.findById(tournamentId).orElse(null);
        if (tournament == null) {
            // Hoặc ném ra một Exception
            return;
        }

        // Xóa danh sách cũ
        tournament.getTeams().clear();

        // Thêm danh sách mới nếu có
        if (teamIds != null && !teamIds.isEmpty()) {
            List<Team> selectedTeams = teamRepository.findAllById(teamIds);
            tournament.setTeams(new HashSet<>(selectedTeams));
        }

        tournamentRepository.save(tournament);
    }
}
