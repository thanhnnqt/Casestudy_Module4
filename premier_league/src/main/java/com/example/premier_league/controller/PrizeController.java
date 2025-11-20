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

    // ... (Các phương thức showList, showCreateForm, showEditForm, save GIỮ NGUYÊN) ...
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
        if (prizeDto.getId() != null) {
            prize = prizeService.findById(prizeDto.getId());
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

        if (prizeDto.getAwardedDate() == null) {
            prizeDto.setAwardedDate(LocalDate.now());
        }

        model.addAttribute("prizeDto", prizeDto);
        model.addAttribute("prizeName", prize.getName());
        // Gửi thêm biến type gốc để View biết đường hiển thị form đúng
        model.addAttribute("prizeType", prize.getType());

        model.addAttribute("teams", teamService.findAll());

        return "prize/prize-award";
    }

    // --- ĐÂY LÀ PHƯƠNG THỨC CẦN SỬA ---
    @PostMapping("/award/save")
    public String saveAward(@ModelAttribute("prizeDto") PrizeDto prizeDto, RedirectAttributes redirect) {
        // 1. Lấy dữ liệu gốc từ DB
        Prize prize = prizeService.findById(prizeDto.getId());
        if (prize == null) {
            redirect.addFlashAttribute("error", "Giải thưởng không tồn tại!");
            return "redirect:/admin/prize";
        }

        // 2. VALIDATE NGHIÊM NGẶT DỰA TRÊN DATABASE (Không tin tưởng prizeDto.getType() từ form)

        // Trường hợp 1: Giải Cá nhân (INDIVIDUAL)
        if ("INDIVIDUAL".equals(prize.getType())) {
            if (prizeDto.getPlayerId() == null) {
                redirect.addFlashAttribute("error", "Lỗi: Đây là giải cá nhân, bắt buộc phải chọn Cầu thủ!");
                return "redirect:/admin/prize/award/" + prizeDto.getId();
            }

            Player player = playerService.findById(prizeDto.getPlayerId());
            if (player == null) {
                redirect.addFlashAttribute("error", "Lỗi: Cầu thủ không tồn tại!");
                return "redirect:/admin/prize/award/" + prizeDto.getId();
            }

            // Gán dữ liệu: Người nhận là Player, Team là team của Player đó
            prize.setPlayer(player);
            prize.setTeam(player.getTeam());
        }

        // Trường hợp 2: Giải Tập thể (TEAM)
        else if ("TEAM".equals(prize.getType())) {
            if (prizeDto.getTeamId() == null) {
                redirect.addFlashAttribute("error", "Lỗi: Đây là giải tập thể, bắt buộc phải chọn Đội bóng!");
                return "redirect:/admin/prize/award/" + prizeDto.getId();
            }

            Team team = teamService.findById(prizeDto.getTeamId());
            if (team == null) {
                redirect.addFlashAttribute("error", "Lỗi: Đội bóng không tồn tại!");
                return "redirect:/admin/prize/award/" + prizeDto.getId();
            }

            // Gán dữ liệu: Người nhận là null, Team là Team được chọn
            prize.setPlayer(null);
            prize.setTeam(team);
        }

        // Trường hợp 3: Khác (OTHER) - Tùy logic của bạn, ở đây giả sử không gán người nhận
        else {
            // Có thể thêm logic cho loại giải khác ở đây nếu cần
        }

        // 3. Cập nhật ngày trao
        prize.setAwardedDate(prizeDto.getAwardedDate());

        prizeService.save(prize);
        redirect.addFlashAttribute("message", "Đã trao giải thành công!");
        return "redirect:/admin/prize";
    }

    // ... (Các phương thức API, delete, revoke giữ nguyên) ...
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

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        prizeService.delete(id);
        redirect.addFlashAttribute("message", "Đã xóa giải thưởng!");
        return "redirect:/admin/prize";
    }

    @GetMapping("/revoke/{id}")
    public String revokeAward(@PathVariable Long id, RedirectAttributes redirect) {
        Prize prize = prizeService.findById(id);
        if (prize != null) {
            prize.setPlayer(null);
            prize.setTeam(null);
            prize.setAwardedDate(null);
            prizeService.save(prize);
            redirect.addFlashAttribute("message", "Đã thu hồi giải thưởng thành công!");
        }
        return "redirect:/admin/prize";
    }
}