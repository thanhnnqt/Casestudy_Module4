package com.example.premier_league.service.impl;

import com.example.premier_league.entity.Match;
import com.example.premier_league.entity.MatchEvent;
import com.example.premier_league.entity.MatchStatus;
import com.example.premier_league.repository.IMatchEventRepository;
import com.example.premier_league.repository.IMatchRepository;
import com.example.premier_league.service.IMatchEventService;
import com.example.premier_league.service.IRankingService;
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
    private final IRankingService rankingService;

    @Override
    @Transactional
    public MatchEvent createEvent(MatchEvent event) {

        // Lấy trận đấu
        Match match = matchRepo.findById(event.getMatchId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy trận đấu"));

        // Kiểm tra trạng thái trận
        if (match.getStatus() == MatchStatus.POSTPONED)
            throw new RuntimeException("Trận đấu bị hoãn, không thể thêm sự kiện");

        if (match.getStatus() == MatchStatus.FINISHED)
            throw new RuntimeException("Trận đấu đã kết thúc, không thể thêm sự kiện");

        // Lưu sự kiện mới
        MatchEvent saved = eventRepo.save(event);

        String type = event.getType() == null ? "" : event.getType().toUpperCase();

        // =========================
        //      XỬ LÝ SỰ KIỆN GHI BÀN
        // =========================
        if (type.equals("GOAL")) {

            if (match.getStatus() != MatchStatus.LIVE)
                throw new RuntimeException("Trận đấu chưa LIVE, không thể thêm GOAL");

            if (event.getTeamId() != null) {
                if (event.getTeamId().equals(match.getHomeTeam().getId())) {
                    match.setHomeScore((match.getHomeScore() == null ? 0 : match.getHomeScore()) + 1);
                }
                if (event.getTeamId().equals(match.getAwayTeam().getId())) {
                    match.setAwayScore((match.getAwayScore() == null ? 0 : match.getAwayScore()) + 1);
                }
            }

            matchRepo.save(match);

            // Gửi score realtime
            messaging.convertAndSend("/topic/match/" + match.getId() + "/score", match);

            // Gửi event realtime để append
            messaging.convertAndSend("/topic/match/" + match.getId() + "/events", saved);

            return saved;
        }

        // =========================
        //      KẾT THÚC TRẬN
        // =========================
        if (type.equals("MATCH_END")) {

            if (match.getStatus() != MatchStatus.LIVE)
                throw new RuntimeException("Trận chưa ở trạng thái LIVE");

            match.setStatus(MatchStatus.FINISHED);
            matchRepo.save(match);

            // Cập nhật BXH
            rankingService.applyMatchResult(match);

            messaging.convertAndSend("/topic/match/" + match.getId() + "/score", match);
            messaging.convertAndSend("/topic/match/" + match.getId() + "/events", saved);
            messaging.convertAndSend("/topic/rankings", rankingService.getRanking());

            return saved;
        }

        // =========================
        //   CÁC SỰ KIỆN KHÁC (THẺ...)
        // =========================
        messaging.convertAndSend("/topic/match/" + match.getId() + "/events", saved);

        return saved;
    }


    @Override
    public List<MatchEvent> listEvents(Long matchId) {
        return eventRepo.findByMatchIdOrderByMinuteAsc(matchId);
    }

    @Override
    public Match findMatchById(Long matchId) {
        return matchRepo.findById(matchId).orElse(null);
    }

    @Override
    public MatchEvent getEvent(Long id) {
        return eventRepo.findById(id).orElse(null);
    }

    @Override
    public List<MatchEvent> getEventsByMatch(Long matchId) {
        return eventRepo.findByMatchIdOrderByMinuteAsc(matchId);
    }
}
