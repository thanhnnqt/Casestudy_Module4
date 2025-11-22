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
@RequestMapping("/admin/prize")
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

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("prizeDto", new PrizeDto());
        return "prize/prize-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Prize prize = prizeService.findById(id);
        if (prize == null) {
            return "redirect:/admin/prize";
        }
        PrizeDto prizeDto = new PrizeDto();
        BeanUtils.copyProperties(prize, prizeDto);

        // MAPPING THỦ CÔNG: Entity.id -> Dto.prizeId
        prizeDto.setPrizeId(prize.getId());

        model.addAttribute("prizeDto", prizeDto);
        return "prize/prize-form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("prizeDto") PrizeDto prizeDto,
                       BindingResult bindingResult,
                       RedirectAttributes redirect) {
        if (bindingResult.hasErrors()) {
            return "prize/prize-form";
        }
        Prize prize;

        // SỬA: Dùng getPrizeId()
        if (prizeDto.getPrizeId() != null) {
            prize = prizeService.findById(prizeDto.getPrizeId());
        } else {
            prize = new Prize();
        }

        prize.setName(prizeDto.getName());
        prize.setType(prizeDto.getType());
        prize.setAmount(prizeDto.getAmount());

        if (prize.getId() == null) {
            prize.setAwardedDate(null);
        }
        prizeService.save(prize);
        redirect.addFlashAttribute("message", "Lưu thông tin giải thưởng thành công!");
        return "redirect:/admin/prize";
    }

    @GetMapping("/award/{id}")
    public String showAwardForm(@PathVariable Long id, Model model) {
        Prize prize = prizeService.findById(id);
        if (prize == null) return "redirect:/admin/prize";

        PrizeDto prizeDto = new PrizeDto();
        BeanUtils.copyProperties(prize, prizeDto);

        // 1. MAPPING THỦ CÔNG ID
        prizeDto.setPrizeId(prize.getId());

        // 2. MAPPING LOGIC NGƯỜI NHẬN (Fix lỗi không hiện người cũ)
        if (prize.getPlayer() != null) {
            prizeDto.setPlayerId(prize.getPlayer().getId());
            model.addAttribute("selectedTeamId", prize.getPlayer().getTeam().getId());
        }
        if (prize.getTeam() != null) {
            prizeDto.setTeamId(prize.getTeam().getId());
        }

        if (prizeDto.getAwardedDate() == null) {
            prizeDto.setAwardedDate(LocalDate.now());
        }

        model.addAttribute("prizeDto", prizeDto);
        model.addAttribute("prizeName", prize.getName());
        model.addAttribute("prizeType", prize.getType());
        model.addAttribute("teams", teamService.findAll());

        return "prize/prize-award";
    }

    @PostMapping("/award/save")
    public String saveAward(@ModelAttribute("prizeDto") PrizeDto prizeDto, RedirectAttributes redirect) {
        // SỬA: Dùng getPrizeId()
        Prize prize = prizeService.findById(prizeDto.getPrizeId());

        if (prize == null) {
            redirect.addFlashAttribute("error", "Giải thưởng không tồn tại!");
            return "redirect:/admin/prize";
        }

        // Validation logic
        if ("INDIVIDUAL".equals(prize.getType())) {
            if (prizeDto.getPlayerId() == null) {
                redirect.addFlashAttribute("error", "Lỗi: Giải cá nhân bắt buộc chọn Cầu thủ!");
                return "redirect:/admin/prize/award/" + prizeDto.getPrizeId(); // SỬA getPrizeId
            }
            Player player = playerService.findById(prizeDto.getPlayerId());
            if (player == null) {
                redirect.addFlashAttribute("error", "Lỗi: Cầu thủ không tồn tại!");
                return "redirect:/admin/prize/award/" + prizeDto.getPrizeId();
            }
            prize.setPlayer(player);
            prize.setTeam(player.getTeam());
        }
        else if ("TEAM".equals(prize.getType())) {
            if (prizeDto.getTeamId() == null) {
                redirect.addFlashAttribute("error", "Lỗi: Giải tập thể bắt buộc chọn Đội bóng!");
                return "redirect:/admin/prize/award/" + prizeDto.getPrizeId();
            }
            Team team = teamService.findById(prizeDto.getTeamId());
            if (team == null) {
                redirect.addFlashAttribute("error", "Lỗi: Đội bóng không tồn tại!");
                return "redirect:/admin/prize/award/" + prizeDto.getPrizeId();
            }
            prize.setPlayer(null);
            prize.setTeam(team);
        }

        prize.setAwardedDate(prizeDto.getAwardedDate());
        prizeService.save(prize);
        redirect.addFlashAttribute("message", "Đã trao giải thành công!");
        return "redirect:/admin/prize";
    }

    @GetMapping("/api/players-by-team/{teamId}")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getPlayersByTeam(@PathVariable Long teamId) {
        List<Player> players = playerService.findByTeamId(teamId);
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

    @GetMapping("/delete/{prizeId}")
    public String delete(@PathVariable("prizeId") Long prizeId, RedirectAttributes redirect) {
        try {
            prizeService.delete(prizeId);
            redirect.addFlashAttribute("message", "Đã xóa giải thưởng!");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Không thể xóa giải thưởng này (có thể do ràng buộc dữ liệu).");
        }
        return "redirect:/admin/prize";
    }

    // 2. Sửa hàm Thu hồi: Đổi 'id' thành 'prizeId'
    @GetMapping("/revoke/{prizeId}")
    public String revokeAward(@PathVariable("prizeId") Long prizeId, RedirectAttributes redirect) {
        Prize prize = prizeService.findById(prizeId);
        if (prize != null) {
            // Xóa thông tin người nhận
            prize.setPlayer(null);
            prize.setTeam(null);
            prize.setAwardedDate(null);
            prizeService.save(prize);
            redirect.addFlashAttribute("message", "Đã thu hồi giải thưởng thành công!");
        }
        return "redirect:/admin/prize";
    }
}