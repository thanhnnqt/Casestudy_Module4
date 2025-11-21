package com.example.premier_league.service.impl;

import com.example.premier_league.dto.PlayerDto;
import com.example.premier_league.dto.PlayerShortDto;
import com.example.premier_league.entity.Player;
import com.example.premier_league.entity.Team;
import com.example.premier_league.repository.IPlayerRepository;
import com.example.premier_league.repository.ITeamRepository;
import com.example.premier_league.service.IPlayerService;
import com.example.premier_league.service.ITeamService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlayerService implements IPlayerService {

    private final IPlayerRepository playerRepository;
    private final ITeamRepository teamRepository;

    public PlayerService(IPlayerRepository playerRepository,ITeamRepository teamRepository) {
        this.playerRepository = playerRepository;
        this.teamRepository = teamRepository;
    }

    @Override
    public List<Player> findByTeamId(Long teamId) {

        return playerRepository.findByTeamId(teamId);
    }

    @Override
    public List<Player> findAllByIds(List<Long> playerIds) {
        return playerRepository.findAllById(playerIds);
    }

    @Override
    public List<Player> findAll() {
        return playerRepository.findAll();
    }

    @Override
    public void save(PlayerDto playerDto) { // Đổi tên biến thành playerDto cho rõ nghĩa

        // 1. Khởi tạo Entity mới
        Player playerEntity = new Player();

        // 2. Copy dữ liệu từ DTO sang Entity
        // Cách nhanh: Dùng BeanUtils (Lưu ý: tên thuộc tính phải giống nhau)
        BeanUtils.copyProperties(playerDto, playerEntity);

        // 3. Xử lý riêng cho Đội bóng (Quan trọng)
        // Vì DTO chỉ chứa teamId (số), còn Entity cần đối tượng Team
        if (playerDto.getTeamId() != null) {
            // Bạn cần Inject ITeamRepository vào Service này để dùng dòng dưới
            Team team = teamRepository.findById(playerDto.getTeamId()).orElse(null);
            playerEntity.setTeam(team);
        }

        // 4. Lưu Entity vào DB
        playerRepository.save(playerEntity);
    }



    @Override
    public Player findById(Long id) {
        return playerRepository.findById(id).orElse(null);
    }

    @Override
    public List<Player> findByName(String name) {
        return List.of();
    }

//    @Override
//    public List<Player> findByName(String name) {
//        return playerRepository.findByTeamId(name);
//    }

    @Override
    public void update(PlayerDto playerDto) {
        // 1. Tìm Entity cũ đang nằm trong Database
        Player existingPlayer = playerRepository.findById(playerDto.getId()).orElse(null);

        if (existingPlayer != null) {
            // 2. Copy dữ liệu từ DTO đè lên Entity cũ
            // Lưu ý: "team" nên được loại bỏ khỏi copyProperties để tránh lỗi Hibernate cũ
            // Chúng ta sẽ set Team thủ công bằng ID cho an toàn
            BeanUtils.copyProperties(playerDto, existingPlayer, "team");

            // 3. Cập nhật Team (nếu có thay đổi)
            if (playerDto.getTeamId() != null) {
                Team teamRef = new Team();
                teamRef.setId(playerDto.getTeamId());
                existingPlayer.setTeam(teamRef);
            }

            // 4. Bây giờ mới gọi save với đối tượng Entity
            playerRepository.save(existingPlayer);
        }
    }

    @Override
    public void delete(Long id) {
        if (playerRepository.existsById(id)) {
            playerRepository.deleteById(id);
        }
    }


    @Override
    public List<PlayerShortDto> getPlayersByTeam(Long teamId) {
        List<Player> players = playerRepository.findByTeamId(teamId);

        return players.stream()
                .map(p -> new PlayerShortDto(
                        p.getId(),
                        p.getName(),
                        p.getPosition(),
                        p.getAvatar(),
                        p.getSeasonYellowCards(),         // Thẻ vàng mùa giải
                        p.getSuspensionMatchesRemaining() // Số trận treo giò
                ))
                .toList();
    }


}


