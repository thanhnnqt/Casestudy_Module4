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

        // Validate match reference
        if (event.getMatch() == null || event.getMatch().getId() == null) {
            throw new RuntimeException("Thiếu matchId trong sự kiện");
        }
        Long matchId = event.getMatch().getId();

        // Load managed match entity
        Match match = matchRepo.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy trận đấu (id=" + matchId + ")"));

        // Resolve team if provided
        if (event.getTeam() != null && event.getTeam().getId() != null) {
            Team team = teamRepo.findById(event.getTeam().getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đội bóng (id=" + event.getTeam().getId() + ")"));
            // ensure team belongs to match
            if (!team.getId().equals(match.getHomeTeam().getId()) && !team.getId().equals(match.getAwayTeam().getId())) {
                throw new RuntimeException("Đội này không thuộc trận đấu");
            }
            event.setTeam(team);
        }

        // Resolve player if provided
        if (event.getPlayer() != null && event.getPlayer().getId() != null) {
            Player player = playerRepo.findById(event.getPlayer().getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy cầu thủ (id=" + event.getPlayer().getId() + ")"));
            event.setPlayer(player);
        }

        // attach managed match
        event.setMatch(match);

        // save event
        MatchEvent saved = eventRepo.save(event);

        // handle GOAL
        if ("GOAL".equalsIgnoreCase(saved.getType())) {

            if (match.getStatus() != MatchStatus.LIVE) {
                // nếu bạn muốn cho phép thêm bàn khi không LIVE thì thay đổi logic này
                throw new RuntimeException("Trận đấu chưa LIVE, không thể thêm bàn thắng");
            }

            if (saved.getTeam() == null || saved.getTeam().getId() == null) {
                throw new RuntimeException("Bàn thắng phải có teamId");
            }

            Long teamId = saved.getTeam().getId();

            if (teamId.equals(match.getHomeTeam().getId())) {
                match.setHomeScore((match.getHomeScore() == null ? 0 : match.getHomeScore()) + 1);
            } else if (teamId.equals(match.getAwayTeam().getId())) {
                match.setAwayScore((match.getAwayScore() == null ? 0 : match.getAwayScore()) + 1);
            } else {
                throw new RuntimeException("Đội này không thuộc trận đấu");
            }

            matchRepo.save(match);

            // gửi realtime cập nhật tỉ số (dưới dạng đơn giản để FE đọc dễ dàng)
            Map<String, Object> scorePayload = new HashMap<>();
            scorePayload.put("homeScore", match.getHomeScore());
            scorePayload.put("awayScore", match.getAwayScore());
            scorePayload.put("status", match.getStatus() != null ? match.getStatus().name() : null);
            messaging.convertAndSend("/topic/match/" + matchId + "/score", scorePayload);
        }

        // ======================== HANDLE YELLOW CARD (season-wide accumulation) ========================
        if ("YELLOW_CARD".equalsIgnoreCase(saved.getType())) {
            Player player = saved.getPlayer();
            if (player != null) {
                // tăng tổng thẻ vàng mùa giải
                player.setSeasonYellowCards(player.getSeasonYellowCards() + 1);

                // Lưu player ngay (để tránh mất khi rollback)
                playerRepo.save(player);

                // Nếu đạt ngưỡng (2 theo yêu cầu) -> phát sinh RED_CARD và treo giò 3 trận
                if (player.getSeasonYellowCards() >= 2) {

                    // reset thẻ vàng mùa giải (theo luật bạn muốn)
                    player.setSeasonYellowCards(0);

                    // đặt treo giò 3 trận
                    player.setSuspensionMatchesRemaining(3);

                    playerRepo.save(player);

                    // tạo thẻ đỏ tự động (sự kiện)
                    MatchEvent red = new MatchEvent();
                    red.setMatch(match);
                    red.setPlayer(player);
                    red.setTeam(saved.getTeam());
                    red.setMinute(saved.getMinute()); // có thể +1 nếu muốn
                    red.setType("RED_CARD");
                    red.setDescription("Nhận thẻ đỏ do tích lũy 2 thẻ vàng trong mùa giải");

                    MatchEvent redSaved = eventRepo.save(red);

                    // gửi realtime thẻ đỏ
                    messaging.convertAndSend("/topic/match/" + matchId + "/events", toDto(redSaved));

                    // (tùy chọn) broadcast bảng xếp hạng / danh sách treo giò nếu cần
                }
            }
        }

        // ======================== HANDLE RED CARD DIRECT ========================
        if ("RED_CARD".equalsIgnoreCase(saved.getType())) {
            Player player = saved.getPlayer();
            if (player != null) {
                // Nếu chưa có suspension hoặc muốn set tối thiểu 3 trận thì đặt
                if (player.getSuspensionMatchesRemaining() < 3) {
                    player.setSuspensionMatchesRemaining(3);
                    playerRepo.save(player);
                }
            }
        }

        // handle MATCH_END event (khi admin gửi loại này)
        if ("MATCH_END".equalsIgnoreCase(saved.getType())) {

            // tránh cập nhật BXH nhiều lần nếu trận đã FINISHED trước đó
            boolean alreadyFinished = match.getStatus() == MatchStatus.FINISHED;

            // cập nhật trạng thái trận
            match.setStatus(MatchStatus.FINISHED);

            int home = match.getHomeScore() == null ? 0 : match.getHomeScore();
            int away = match.getAwayScore() == null ? 0 : match.getAwayScore();

            // nếu trận chưa được xử lý FINISH trước đó => cập nhật thống kê đội
            if (!alreadyFinished) {
                Team homeTeam = teamRepo.findById(match.getHomeTeam().getId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy đội nhà"));
                Team awayTeam = teamRepo.findById(match.getAwayTeam().getId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy đội khách"));

                // Cập nhật bàn thắng / bàn thua mùa giải
                homeTeam.setGoalsFor(homeTeam.getGoalsFor() + home);
                homeTeam.setGoalsAgainst(homeTeam.getGoalsAgainst() + away);

                awayTeam.setGoalsFor(awayTeam.getGoalsFor() + away);
                awayTeam.setGoalsAgainst(awayTeam.getGoalsAgainst() + home);

                // Cập nhật thắng/hòa/thua và điểm
                if (home > away) {
                    homeTeam.setWinCount(homeTeam.getWinCount() + 1);
                    homeTeam.setPoints(homeTeam.getPoints() + 3);

                    awayTeam.setLoseCount(awayTeam.getLoseCount() + 1);
                } else if (home < away) {
                    awayTeam.setWinCount(awayTeam.getWinCount() + 1);
                    awayTeam.setPoints(awayTeam.getPoints() + 3);

                    homeTeam.setLoseCount(homeTeam.getLoseCount() + 1);
                } else {
                    homeTeam.setDrawCount(homeTeam.getDrawCount() + 1);
                    awayTeam.setDrawCount(awayTeam.getDrawCount() + 1);

                    homeTeam.setPoints(homeTeam.getPoints() + 1);
                    awayTeam.setPoints(awayTeam.getPoints() + 1);
                }

                // Cập nhật hiệu số
                homeTeam.setGoalDifference(homeTeam.getGoalsFor() - homeTeam.getGoalsAgainst());
                awayTeam.setGoalDifference(awayTeam.getGoalsFor() - awayTeam.getGoalsAgainst());

                // Lưu 2 đội
                teamRepo.save(homeTeam);
                teamRepo.save(awayTeam);
            }

            // Lưu trận (trạng thái + tỉ số)
            matchRepo.save(match);

            // gửi realtime cập nhật status + score
            Map<String, Object> finishPayload = new HashMap<>();
            finishPayload.put("homeScore", match.getHomeScore());
            finishPayload.put("awayScore", match.getAwayScore());
            finishPayload.put("status", match.getStatus().name());
            messaging.convertAndSend("/topic/match/" + matchId + "/score", finishPayload);

            // gửi realtime sự kiện MATCH_END
            messaging.convertAndSend("/topic/match/" + matchId + "/events", toDto(saved));

            // ======================== GIẢM SỐ TRẬN TREO GIÒ CHO 2 ĐỘI ========================
            // Giảm 1 trận còn lại cho mọi cầu thủ thuộc 2 đội (nếu > 0)
            try {
                List<Player> homePlayers = playerRepo.findByTeamId(match.getHomeTeam().getId());
                List<Player> awayPlayers = playerRepo.findByTeamId(match.getAwayTeam().getId());

                for (Player p : homePlayers) {
                    if (p.getSuspensionMatchesRemaining() > 0) {
                        p.setSuspensionMatchesRemaining(p.getSuspensionMatchesRemaining() - 1);
                        playerRepo.save(p);
                    }
                }
                for (Player p : awayPlayers) {
                    if (p.getSuspensionMatchesRemaining() > 0) {
                        p.setSuspensionMatchesRemaining(p.getSuspensionMatchesRemaining() - 1);
                        playerRepo.save(p);
                    }
                }
            } catch (Exception ex) {
                // không block flow nếu repo không có method findByTeamId
                // (nếu cần, hãy thêm method findByTeamId vào IPlayerRepository)
            }

            // Optionally: broadcast ranking update (if frontend listens)
            // messaging.convertAndSend("/topic/rankings", ...); // nếu có DTO BXH

            // return luôn để không chạy xuống dưới nữa
            return saved;
        }

        // sự kiện bình thường → gửi đây
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

        var match = matchRepo.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy trận đấu"));

        MatchEvent event = new MatchEvent();
        event.setMatch(match);
        // Nếu sự kiện đầu tiên trong trận -> tự động chuyển sang LIVE
        if (match.getStatus() != MatchStatus.LIVE && !"MATCH_END".equalsIgnoreCase(dto.getType())) {
            match.setStatus(MatchStatus.LIVE);
            matchRepo.save(match);
        }
        event.setMinute(dto.getMinute());
        event.setType(dto.getType());
        event.setDescription(dto.getDescription());

        // set team
        if (dto.getTeamId() != null) {
            var team = teamRepo.findById(dto.getTeamId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đội bóng"));
            // kiểm tra team thuộc match
            if (!team.getId().equals(match.getHomeTeam().getId()) &&
                    !team.getId().equals(match.getAwayTeam().getId())) {
                throw new RuntimeException("Đội bóng không đá trận này");
            }
            event.setTeam(team);
        }

        // set player nếu có
        if (dto.getPlayerId() != null) {
            var player = playerRepo.findById(dto.getPlayerId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy cầu thủ"));
            event.setPlayer(player);
        }

        // reuse createEvent để lưu + update score + websocket
        createEvent(event);
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
