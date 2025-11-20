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

    // 1. Hiển thị danh sách
    @GetMapping
    public String showList(Model model) {
        model.addAttribute("prizes", prizeService.findAll());
        return "prize/prize-list";
    }

    // 2. Form Tạo mới (Chỉ nhập thông tin cơ bản)
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("prizeDto", new PrizeDto());
        return "prize/prize-form";
    }

    // 3. Form Sửa (Chỉ sửa thông tin cơ bản)
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Prize prize = prizeService.findById(id);
        if (prize == null) {
            return "redirect:/prize";
        }
        PrizeDto prizeDto = new PrizeDto();
        BeanUtils.copyProperties(prize, prizeDto);
        model.addAttribute("prizeDto", prizeDto);
        return "prize/prize-form";
    }

    // 4. Lưu thông tin cơ bản (Tên, Loại, Tiền)
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

        // Chỉ cập nhật thông tin cơ bản, giữ nguyên người nhận giải (nếu đã có)
        prize.setName(prizeDto.getName());
        prize.setType(prizeDto.getType());
        prize.setAmount(prizeDto.getAmount());

        // Nếu tạo mới thì set ngày trao mặc định là null
        if (prize.getId() == null) {
            prize.setAwardedDate(null);
        }

        prizeService.save(prize);
        redirect.addFlashAttribute("message", "Lưu thông tin giải thưởng thành công!");
        return "redirect:/prize";
    }

    // 5. Form Trao giải (Chọn người/đội nhận)
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

        return "prize/prize-award";
    }

    // 6. Lưu người nhận giải
    @PostMapping("/award/save")
    public String saveAward(@ModelAttribute("prizeDto") PrizeDto prizeDto, RedirectAttributes redirect) {
        Prize prize = prizeService.findById(prizeDto.getId());

        // Cập nhật ngày trao
        prize.setAwardedDate(prizeDto.getAwardedDate());

        // Reset người nhận cũ
        prize.setPlayer(null);
        prize.setTeam(null);

        // Logic gán người nhận dựa trên loại (Individual/Team)
        if ("INDIVIDUAL".equals(prizeDto.getType())) {
            if (prizeDto.getPlayerId() != null) {
                Player player = playerService.findById(prizeDto.getPlayerId());
                if (player != null) {
                    prize.setPlayer(player);
                    prize.setTeam(player.getTeam());
                }
            }
        } else {
            if (prizeDto.getTeamId() != null) {
                Team team = teamService.findById(prizeDto.getTeamId());
                prize.setTeam(team);
            }
        }

        prizeService.save(prize);
        redirect.addFlashAttribute("message", "Đã trao giải thành công!");
        return "redirect:/prize";
    }

    // 7. API lấy cầu thủ theo đội (cho AJAX)
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

    // 8. Xóa giải thưởng
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        prizeService.delete(id);
        redirect.addFlashAttribute("message", "Đã xóa giải thưởng!");
        return "redirect:/prize";
    }
}