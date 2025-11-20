package com.example.premier_league.service.impl;

import com.example.premier_league.entity.Team;
import com.example.premier_league.entity.Tournament;
import com.example.premier_league.repository.ITeamRepository;
import com.example.premier_league.repository.ITournamentRepository;
import com.example.premier_league.service.ITournamentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    /**
     * Kiểm tra giải đấu đã bắt đầu chưa
     */
    @Override
    public boolean isTournamentStarted(Long tournamentId) {
        Tournament t = findById(tournamentId);
        if (t == null) return false;
        // Nếu ngày hiện tại >= ngày bắt đầu -> Đã bắt đầu (TRUE)
        return !LocalDate.now().isBefore(t.getStartDate());
    }

    @Override
    @Transactional
    public void updateTeamsForTournament(Long tournamentId, List<Long> teamIds) {
        Tournament tournament = findById(tournamentId);
        if (tournament == null) throw new RuntimeException("Không tìm thấy giải đấu!");

        // Check logic: Nếu giải đã bắt đầu thì chặn luôn ở Service cho an toàn
        if (isTournamentStarted(tournamentId)) {
            throw new RuntimeException("Giải đấu đã bắt đầu, không thể thay đổi danh sách đội!");
        }

        tournament.getTeams().clear(); // Xóa danh sách cũ

        if (teamIds != null && !teamIds.isEmpty()) {
            List<Team> selectedTeams = teamRepository.findAllById(teamIds);
            tournament.setTeams(new HashSet<>(selectedTeams));
        }
        tournamentRepository.save(tournament);
    }

    /**
     * Chức năng Copy danh sách đội từ giải đấu khác
     */
    @Override
    @Transactional
    public void copyTeamsFromTournament(Long targetTournamentId, Long sourceTournamentId) {
        Tournament target = findById(targetTournamentId);
        Tournament source = findById(sourceTournamentId);

        if (target == null || source == null) {
            throw new RuntimeException("Giải đấu không hợp lệ");
        }

        if (isTournamentStarted(targetTournamentId)) {
            throw new RuntimeException("Giải đấu đích đã bắt đầu, không thể thay đổi!");
        }

        // Lấy danh sách đội từ giải cũ và gán sang giải mới
        Set<Team> sourceTeams = source.getTeams();
        target.setTeams(new HashSet<>(sourceTeams)); // Tạo HashSet mới để tránh tham chiếu trùng

        tournamentRepository.save(target);
    }

    @Override
    public boolean existsBySeason(String season) {
        return tournamentRepository.existsBySeason(season);
    }
}
