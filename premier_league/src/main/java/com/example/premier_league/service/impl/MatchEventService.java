package com.example.premier_league.service.impl;

import com.example.premier_league.dto.MatchEventDto;
import com.example.premier_league.entity.*;
import com.example.premier_league.repository.IMatchEventRepository;
import com.example.premier_league.repository.IMatchRepository;
import com.example.premier_league.repository.IPlayerRepository;
import com.example.premier_league.repository.ITeamRepository;
import com.example.premier_league.service.IMatchEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchEventService implements IMatchEventService {

    private final IMatchEventRepository eventRepo;
    private final IMatchRepository matchRepo;
    private final SimpMessagingTemplate messaging;
    private final ITeamRepository teamRepo;
    private final IPlayerRepository playerRepo;


    @Override
    @Transactional
    public MatchEvent createEvent(MatchEvent event) {

        // Lấy id trận đấu từ entity Match
        if (event.getMatch() == null || event.getMatch().getId() == null) {
            throw new RuntimeException("Thiếu matchId trong sự kiện");
        }
        Long matchId = event.getMatch().getId();

        // Lấy trận đấu từ DB
        Match match = matchRepo.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy trận đấu"));

        // Nếu có đội -> lấy đội từ DB
        if (event.getTeam() != null && event.getTeam().getId() != null) {
            Team team = teamRepo.findById(event.getTeam().getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đội bóng"));
            event.setTeam(team);
        }

        // Nếu có cầu thủ -> lấy cầu thủ từ DB
        if (event.getPlayer() != null && event.getPlayer().getId() != null) {
            Player player = playerRepo.findById(event.getPlayer().getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy cầu thủ"));
            event.setPlayer(player);
        }

        // Gán match managed (từ DB) vào event
        event.setMatch(match);

        // Lưu sự kiện
        MatchEvent saved = eventRepo.save(event);

        // Nếu là bàn thắng thì cập nhật tỉ số
        if ("GOAL".equalsIgnoreCase(saved.getType())) {

            if (match.getStatus() != MatchStatus.LIVE) {
                throw new RuntimeException("Trận đấu chưa LIVE, không thể thêm bàn thắng");
            }

            Long teamId = saved.getTeam().getId();

            if (teamId.equals(match.getHomeTeam().getId())) {
                match.setHomeScore(match.getHomeScore() + 1);
            } else if (teamId.equals(match.getAwayTeam().getId())) {
                match.setAwayScore(match.getAwayScore() + 1);
            } else {
                throw new RuntimeException("Đội này không thuộc trận đấu");
            }

            matchRepo.save(match);

            // Gửi realtime cập nhật tỉ số
            messaging.convertAndSend("/topic/match/" + matchId + "/score", match);
        }

        // Gửi realtime sự kiện mới
        messaging.convertAndSend("/topic/match/" + matchId + "/events", saved);

        return saved;
    }


    @Override
    public List<MatchEvent> listEvents(Long matchId) {
        return eventRepo.findByMatchIdOrderByMinuteAsc(matchId);
    }

    @Override
    public List<MatchEvent> getEventsByMatch(Long matchId) {
        return eventRepo.findByMatchIdOrderByMinuteAsc(matchId);
    }

    @Override
    @Transactional
    public void addEvent(Long matchId, MatchEventDto dto) {

        var match = matchRepo.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));

        MatchEvent event = new MatchEvent();
        event.setMatch(match);
        event.setMinute(dto.getMinute());
        event.setType(dto.getType());
        event.setDescription(dto.getDescription());

        // set team
        if (dto.getTeamId() != null) {
            var team = teamRepo.findById(dto.getTeamId())
                    .orElseThrow(() -> new RuntimeException("Team not found"));
            // kiểm tra team thuộc match
            if (!team.getId().equals(match.getHomeTeam().getId()) &&
                    !team.getId().equals(match.getAwayTeam().getId())) {
                throw new RuntimeException("Team does not belong to this match");
            }
            event.setTeam(team);
        }

        // set player nếu có
        if (dto.getPlayerId() != null) {
            var player = playerRepo.findById(dto.getPlayerId())
                    .orElseThrow(() -> new RuntimeException("Player not found"));
            event.setPlayer(player);
        }

        // tái sử dụng createEvent để lưu + update score + websocket
        createEvent(event);
    }



    @Override
    public Match findMatchById(Long matchId) {
        return matchRepo.findById(matchId).orElse(null);
    }

    @Override
    public MatchEvent getEvent(Long id) {
        return null;
    }
}

