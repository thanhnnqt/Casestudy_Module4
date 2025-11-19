package com.example.premier_league.controller;

import com.example.premier_league.dto.PrizeDto;
import com.example.premier_league.entity.Player;
import com.example.premier_league.entity.Prize;
import com.example.premier_league.entity.Team;
import com.example.premier_league.service.IPlayerService;
import com.example.premier_league.service.IPrizeService;
import com.example.premier_league.service.ITeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/prize")
@RequiredArgsConstructor
public class PrizeController {

    private final IPrizeService prizeService;
    private final ITeamService teamService;
    private final IPlayerService playerService;

    @GetMapping
    public String showList(Model model) {
        model.addAttribute("prizes", prizeService.findAll());
        return "prize/prize-list";
    }

    // --- BƯỚC 1: TẠO GIẢI THƯỞNG (CHƯA TRAO) ---
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("prizeDto", new PrizeDto());
        return "prize/prize-form"; // Form này chỉ nhập tên, tiền, loại
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("prizeDto") PrizeDto prizeDto,
                       BindingResult bindingResult,
                       RedirectAttributes redirect) {
        if (bindingResult.hasErrors()) {
            return "prize/prize-form";
        }

        Prize prize;
        if (prizeDto.getId() != null) {
            prize = prizeService.findById(prizeDto.getId());
        } else {
            prize = new Prize();
        }

        // Chỉ copy các thông tin cơ bản, KHÔNG copy teamId/playerId
        prize.setName(prizeDto.getName());
        prize.setType(prizeDto.getType());
        prize.setAmount(prizeDto.getAmount());

        // Nếu chưa trao thì ngày trao là null
        if(prize.getAwardedDate() == null) {
            prize.setAwardedDate(null);
        }

        prizeService.save(prize);
        redirect.addFlashAttribute("message", "Lưu thông tin giải thưởng thành công!");
        return "redirect:/prize";
    }

    // --- API MỚI: LẤY CẦU THỦ THEO ĐỘI (Dùng cho AJAX) ---
    @GetMapping("/api/players-by-team/{teamId}")
    @ResponseBody // Quan trọng: Trả về JSON thay vì HTML
    public ResponseEntity<List<Map<String, Object>>> getPlayersByTeam(@PathVariable Long teamId) {
        List<Player> players = playerService.findByTeamId(teamId);

        // Chuyển sang list Map đơn giản để tránh lỗi vòng lặp JSON
        List<Map<String, Object>> result = new ArrayList<>();
        for (Player p : players) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", p.getId());
            map.put("name", p.getName());
            map.put("position", p.getPosition());
            result.add(map);
        }
        return ResponseEntity.ok(result);
    }

    // --- CẬP NHẬT: TRAO GIẢI ---
    @GetMapping("/award/{id}")
    public String showAwardForm(@PathVariable Long id, Model model) {
        Prize prize = prizeService.findById(id);
        if (prize == null) return "redirect:/prize";

        PrizeDto prizeDto = new PrizeDto();
        BeanUtils.copyProperties(prize, prizeDto);

        if (prizeDto.getAwardedDate() == null) {
            prizeDto.setAwardedDate(LocalDate.now());
        }

        model.addAttribute("prizeDto", prizeDto);
        model.addAttribute("prizeName", prize.getName());
        model.addAttribute("teams", teamService.findAll());
        // Không cần gửi players nữa vì sẽ load bằng Ajax

        return "prize/prize-award";
    }

    @PostMapping("/award/save")
    public String saveAward(@ModelAttribute("prizeDto") PrizeDto prizeDto, RedirectAttributes redirect) {
        Prize prize = prizeService.findById(prizeDto.getId());
        prize.setAwardedDate(prizeDto.getAwardedDate());

        // Reset người nhận cũ
        prize.setPlayer(null);
        prize.setTeam(null);

        // Kiểm tra loại giải dựa trên input hidden 'type' từ form
        if ("INDIVIDUAL".equals(prizeDto.getType())) {
            // Logic trao giải cá nhân
            if (prizeDto.getPlayerId() != null) {
                Player player = playerService.findById(prizeDto.getPlayerId());
                if (player != null) {
                    prize.setPlayer(player);
                    prize.setTeam(player.getTeam());
                }
            }
        } else {
            // Logic trao giải tập thể
            if (prizeDto.getTeamId() != null) {
                Team team = teamService.findById(prizeDto.getTeamId());
                prize.setTeam(team);
            }
        }

        prizeService.save(prize);
        redirect.addFlashAttribute("message", "Đã trao giải thành công!");
        return "redirect:/prize";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        prizeService.delete(id);
        redirect.addFlashAttribute("message", "Đã xóa giải thưởng!");
        return "redirect:/prize";
    }
}