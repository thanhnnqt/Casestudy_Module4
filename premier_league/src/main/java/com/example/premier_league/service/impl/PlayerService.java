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
    public void update(Player player) {
        if (playerRepository.existsById(player.getId())) {
            playerRepository.save(player);
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
        return playerRepository.findByTeamId(teamId)
                .stream()
                .map(p -> {
                    PlayerShortDto dto = new PlayerShortDto();
                    dto.setId(p.getId());
                    dto.setName(p.getName());
                    dto.setPosition(p.getPosition());
                    return dto;
                })
                .toList();
    }


}
