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
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MatchEventService implements IMatchEventService {

    private final IMatchEventRepository eventRepo;
    private final IMatchRepository matchRepo;
    private final SimpMessagingTemplate messaging;
    private final ITeamRepository teamRepo;
    private final IPlayerRepository playerRepo;
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

        Long matchId = Optional.ofNullable(event.getMatch())
                .orElseThrow(() -> new RuntimeException("Thiếu match reference"))
                .getId();

        Match match = matchRepo.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy trận"));

        // resolve team (nếu có)
        if (event.getTeam() != null && event.getTeam().getId() != null) {
            Team t = teamRepo.findById(event.getTeam().getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy team"));
            // kiểm tra team thuộc match
            if (!t.getId().equals(match.getHomeTeam().getId()) && !t.getId().equals(match.getAwayTeam().getId())) {
                throw new RuntimeException("Đội này không thuộc trận đấu");
            }
            event.setTeam(t);
        }

        // resolve player (nếu có) và kiểm tra treo giò
        if (event.getPlayer() != null && event.getPlayer().getId() != null) {
            Player p = playerRepo.findById(event.getPlayer().getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy cầu thủ"));

            // đảm bảo không add sự kiện cho cầu thủ đang bị treo
            if (p.getSuspensionMatchesRemaining() != null && p.getSuspensionMatchesRemaining() > 0) {
                throw new RuntimeException("Cầu thủ " + p.getName() + " đang bị treo (" + p.getSuspensionMatchesRemaining() + " trận còn lại)");
            }

            event.setPlayer(p);
        }

        event.setMatch(match);
        MatchEvent saved = eventRepo.save(event);

        // ============================ GOAL =============================
        if ("GOAL".equalsIgnoreCase(saved.getType())) {

            // nếu chưa LIVE -> set LIVE (hợp lý khi admin bắt đầu trận bằng event)
            if (match.getStatus() != MatchStatus.LIVE) {
                match.setStatus(MatchStatus.LIVE);
            }

            Long tid = saved.getTeam().getId();
            if (tid.equals(match.getHomeTeam().getId())) {
                match.setHomeScore((match.getHomeScore() == null ? 0 : match.getHomeScore()) + 1);
            } else {
                match.setAwayScore((match.getAwayScore() == null ? 0 : match.getAwayScore()) + 1);
            }

            matchRepo.save(match);

            Map<String, Object> score = new HashMap<>();
            score.put("homeScore", match.getHomeScore());
            score.put("awayScore", match.getAwayScore());
            score.put("status", match.getStatus().name());

            messaging.convertAndSend("/topic/match/" + matchId + "/score", score);
        }

        // ============================ YELLOW CARD (accumulation) =============================
        if ("YELLOW_CARD".equalsIgnoreCase(saved.getType())) {
            Player player = saved.getPlayer();
            if (player != null) {
                // safeguard nulls
                Integer current = player.getSeasonYellowCards() == null ? 0 : player.getSeasonYellowCards();
                current++;
                player.setSeasonYellowCards(current);

                // lưu ngay
                playerRepo.save(player);

                // nếu đạt ngưỡng = 2 -> treo giò 1 trận theo yêu cầu
                if (current >= 2) {
                    player.setSeasonYellowCards(0); // reset sau khi kỷ luật
                    Integer remain = player.getSuspensionMatchesRemaining() == null ? 0 : player.getSuspensionMatchesRemaining();
                    player.setSuspensionMatchesRemaining(remain + 1); // treo 1 trận
                    playerRepo.save(player);

                    // tạo event thông báo suspension (dùng để FE hiển thị)
                    MatchEvent susp = new MatchEvent();
                    susp.setMatch(match);
                    susp.setPlayer(player);
                    susp.setTeam(saved.getTeam());
                    susp.setMinute(saved.getMinute());
                    susp.setType("SUSPENSION");
                    susp.setDescription("Treo giò 1 trận do tích lũy 2 thẻ vàng trong mùa giải");
                    MatchEvent suspSaved = eventRepo.save(susp);
                    messaging.convertAndSend("/topic/match/" + matchId + "/events", toDto(suspSaved));
                }
            }
        }

        // ============================ RED CARD (direct) =============================
        if ("RED_CARD".equalsIgnoreCase(saved.getType())) {
            Player player = saved.getPlayer();
            if (player != null) {
                Integer remain = player.getSuspensionMatchesRemaining() == null ? 0 : player.getSuspensionMatchesRemaining();
                // đảm bảo treo tối thiểu 3 trận
                if (remain < 3) {
                    player.setSuspensionMatchesRemaining(3);
                    playerRepo.save(player);
                }
            }
        }

        // ============================ MATCH END =============================
        if ("MATCH_END".equalsIgnoreCase(saved.getType())) {

            // Nếu trận đã FINISHED trước đó → bỏ qua cập nhật BXH (tránh cộng dồn)
            if (match.getStatus() != MatchStatus.FINISHED) {
                // chuyển trạng thái
                match.setStatus(MatchStatus.FINISHED);
                matchRepo.save(match);

                // CẬP NHẬT BXH duy nhất tại đây (RankingsService xử lý cộng điểm/hieu so)
                rankingsService.applyMatchResult(match);
            } else {
                // nếu đã finished, vẫn update trạng thái/tỉ số send cho FE nhưng không cập nhật BXH
                matchRepo.save(match);
            }

            // gửi realtime score + event MATCH_END
            Map<String, Object> finish = new HashMap<>();
            finish.put("homeScore", match.getHomeScore());
            finish.put("awayScore", match.getAwayScore());
            finish.put("status", match.getStatus().name());
            messaging.convertAndSend("/topic/match/" + matchId + "/score", finish);
            messaging.convertAndSend("/topic/match/" + matchId + "/events", toDto(saved));

            // Sau khi trận kết thúc → giảm 1 trận treo giò cho mọi cầu thủ 2 đội (nếu repository hỗ trợ)
            try {
                List<Player> homePlayers = playerRepo.findByTeamId(match.getHomeTeam().getId());
                List<Player> awayPlayers = playerRepo.findByTeamId(match.getAwayTeam().getId());

                for (Player p : homePlayers) {
                    if (p.getSuspensionMatchesRemaining() != null && p.getSuspensionMatchesRemaining() > 0) {
                        p.setSuspensionMatchesRemaining(p.getSuspensionMatchesRemaining() - 1);
                        playerRepo.save(p);
                    }
                }
                for (Player p : awayPlayers) {
                    if (p.getSuspensionMatchesRemaining() != null && p.getSuspensionMatchesRemaining() > 0) {
                        p.setSuspensionMatchesRemaining(p.getSuspensionMatchesRemaining() - 1);
                        playerRepo.save(p);
                    }
                }
            } catch (Exception ex) {
                // nếu IPlayerRepository không có findByTeamId thì không block flow
            }

            return saved;
        }

        // sự kiện thường (không phải MATCH_END)
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

        // nếu bắt đầu trận bằng event (first event) -> set LIVE
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
            // kiểm tra team thuộc match
            if (!t.getId().equals(match.getHomeTeam().getId()) && !t.getId().equals(match.getAwayTeam().getId())) {
                throw new RuntimeException("Đội bóng không đá trận này");
            }
            e.setTeam(t);
        }

        if (dto.getPlayerId() != null) {
            Player p = playerRepo.findById(dto.getPlayerId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy cầu thủ"));

            // chặn nếu đang treo giò
            if (p.getSuspensionMatchesRemaining() != null && p.getSuspensionMatchesRemaining() > 0) {
                throw new RuntimeException("Cầu thủ " + p.getName() + " đang bị treo (" + p.getSuspensionMatchesRemaining() + " trận còn lại)");
            }

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
}
