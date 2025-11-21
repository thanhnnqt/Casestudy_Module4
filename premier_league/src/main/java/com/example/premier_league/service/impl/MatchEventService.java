package com.example.premier_league.service.impl;

import com.example.premier_league.dto.MatchEventDto;
import com.example.premier_league.dto.MatchEventResponse;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MatchEventService implements IMatchEventService {

    private final IMatchEventRepository eventRepo;
    private final IMatchRepository matchRepo;
    private final SimpMessagingTemplate messaging;
    private final ITeamRepository teamRepo;
    private final IPlayerRepository playerRepo;

    // RankingsService để broadcast BXH realtime (nhưng không để MatchEventService cập nhật điểm trực tiếp)
    private final RankingsService rankingsService;

    private MatchEventResponse toDto(MatchEvent e) {
        return new MatchEventResponse(
                e.getId(),
                e.getMinute(),
                e.getType(),
                e.getDescription(),
                e.getTeam() != null ? e.getTeam().getName() : null,
                e.getPlayer() != null ? e.getPlayer().getName() : null
        );
    }

    @Override
    @Transactional
    public MatchEvent createEvent(MatchEvent event) {

        if (event.getMatch() == null || event.getMatch().getId() == null) {
            throw new RuntimeException("Thiếu matchId trong sự kiện");
        }
        Long matchId = event.getMatch().getId();

        Match match = matchRepo.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy trận đấu"));

        // set team
        if (event.getTeam() != null && event.getTeam().getId() != null) {
            Team team = teamRepo.findById(event.getTeam().getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy team"));
            if (!team.getId().equals(match.getHomeTeam().getId())
                    && !team.getId().equals(match.getAwayTeam().getId())) {
                throw new RuntimeException("Đội này không thuộc trận đấu");
            }
            event.setTeam(team);
        }

        // set player
        if (event.getPlayer() != null && event.getPlayer().getId() != null) {
            Player p = playerRepo.findById(event.getPlayer().getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy cầu thủ"));
            event.setPlayer(p);
        }

        event.setMatch(match);
        MatchEvent saved = eventRepo.save(event);

        // ============================ GOAL =============================
        if ("GOAL".equalsIgnoreCase(saved.getType())) {
            if (match.getStatus() != MatchStatus.LIVE) {
                throw new RuntimeException("Trận đấu chưa LIVE");
            }

            Long scoringTeam = saved.getTeam().getId();
            if (scoringTeam.equals(match.getHomeTeam().getId())) {
                match.setHomeScore((match.getHomeScore() == null ? 0 : match.getHomeScore()) + 1);
            } else if (scoringTeam.equals(match.getAwayTeam().getId())) {
                match.setAwayScore((match.getAwayScore() == null ? 0 : match.getAwayScore()) + 1);
            } else {
                throw new RuntimeException("Đội ghi bàn không hợp lệ cho trận");
            }

            matchRepo.save(match);

            Map<String, Object> score = new HashMap<>();
            score.put("homeScore", match.getHomeScore());
            score.put("awayScore", match.getAwayScore());
            score.put("status", match.getStatus() != null ? match.getStatus().name() : null);

            messaging.convertAndSend("/topic/match/" + matchId + "/score", score);
        }

        // ============================ MATCH END =============================
        if ("MATCH_END".equalsIgnoreCase(saved.getType())) {

            boolean alreadyFinished = match.getStatus() == MatchStatus.FINISHED;

            match.setStatus(MatchStatus.FINISHED);
            matchRepo.save(match);

            // send final score + event
            Map<String, Object> finish = new HashMap<>();
            finish.put("homeScore", match.getHomeScore());
            finish.put("awayScore", match.getAwayScore());
            finish.put("status", match.getStatus().name());
            messaging.convertAndSend("/topic/match/" + matchId + "/score", finish);
            messaging.convertAndSend("/topic/match/" + matchId + "/events", toDto(saved));

            // only call rankingsService once (applyMatchResult is idempotent)
            if (!alreadyFinished) {
                rankingsService.applyMatchResult(match);
            }
            return saved;
        }

        // ============================ NORMAL EVENT =============================
        messaging.convertAndSend("/topic/match/" + matchId + "/events", toDto(saved));
        return saved;
    }

    @Override
    public List<MatchEvent> listEvents(Long matchId) {
        return eventRepo.findByMatchIdOrderByMinuteAsc(matchId);
    }

    @Override
    public List<MatchEventResponse> getEventsByMatch(Long matchId) {
        return eventRepo.findByMatchIdOrderByMinuteAsc(matchId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void addEvent(Long matchId, MatchEventDto dto) {

        Match match = matchRepo.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy trận"));

        MatchEvent e = new MatchEvent();
        e.setMatch(match);

        if (match.getStatus() != MatchStatus.LIVE &&
                !"MATCH_END".equalsIgnoreCase(dto.getType())) {

            match.setStatus(MatchStatus.LIVE);
            matchRepo.save(match);
        }

        e.setMinute(dto.getMinute());
        e.setType(dto.getType());
        e.setDescription(dto.getDescription());

        if (dto.getTeamId() != null) {
            Team t = teamRepo.findById(dto.getTeamId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đội bóng"));
            e.setTeam(t);
        }

        if (dto.getPlayerId() != null) {
            Player p = playerRepo.findById(dto.getPlayerId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy cầu thủ"));
            e.setPlayer(p);
        }

        createEvent(e);
    }

    @Override
    public Match findMatchById(Long matchId) {
        return matchRepo.findById(matchId).orElse(null);
    }

    @Override
    public MatchEvent getEvent(Long id) {
        return eventRepo.findById(id).orElse(null);
    }

    /**
     * Phương thức xử lý logic cộng dồn thẻ vàng theo luật Premier League
     * Gọi phương thức này bên trong createEvent khi loại sự kiện là YELLOW_CARD
     */
    private void applySuspensionRules(Player player, int currentRound) {
        int currentYellows = player.getSeasonYellowCards();

        // Luật 1: 5 thẻ vàng trước vòng 19 -> Treo giò 1 trận
        if (currentYellows == 5 && currentRound <= 19) {
            player.setSuspensionMatchesRemaining(player.getSuspensionMatchesRemaining() + 1);
        }
        // Luật 2: 10 thẻ vàng trước vòng 32 -> Treo giò 2 trận
        else if (currentYellows == 10 && currentRound <= 32) {
            player.setSuspensionMatchesRemaining(player.getSuspensionMatchesRemaining() + 2);
        }
        // Luật 3: 15 thẻ vàng (bất kể vòng nào) -> Treo giò 3 trận
        else if (currentYellows == 15) {
            player.setSuspensionMatchesRemaining(player.getSuspensionMatchesRemaining() + 3);
        }

        // Lưu cập nhật
        playerRepo.save(player);
    }

}
