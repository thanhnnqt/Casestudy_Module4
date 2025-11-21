package com.example.premier_league.controller;

import com.example.premier_league.dto.PlayerDto;
import com.example.premier_league.entity.Account;
import com.example.premier_league.entity.Player;
import com.example.premier_league.entity.Team;
import com.example.premier_league.service.IAccountService;
import com.example.premier_league.service.IPlayerService;
import com.example.premier_league.service.ITeamService;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/admin/players")
public class PlayerController {

    private final IPlayerService playerService;
    private final ITeamService teamService;
    private final IAccountService accountService;

    public PlayerController(IPlayerService playerService, ITeamService teamService, IAccountService accountService) {
        this.playerService = playerService;
        this.teamService = teamService;
        this.accountService = accountService;
    }

    // 1. HIỂN THỊ DANH SÁCH (Chỉ hiện cầu thủ của đội mình)
    @GetMapping
    public String listPlayers(Model model, Principal principal) {
        Team myTeam = getOwnerTeam(principal);
        if (myTeam == null) return "redirect:/login"; // Chưa có đội hoặc lỗi

        List<Player> players = playerService.findByTeamId(myTeam.getId());

        model.addAttribute("players", players);
        model.addAttribute("myTeam", myTeam); // Để hiển thị tên đội trên giao diện
        return "player/list";
    }

    // 2. FORM TẠO MỚI
    @GetMapping("/create")
    public String showCreateForm(Model model, Principal principal) {
        Team myTeam = getOwnerTeam(principal);

        PlayerDto playerDto = new PlayerDto();
        // Tự động gán ID đội của Owner vào DTO
        playerDto.setTeamId(myTeam.getId());

        model.addAttribute("playerDto", playerDto);
        model.addAttribute("myTeamName", myTeam.getName()); // Để hiển thị tên đội (readonly)
        return "player/create";
    }

    // 3. XỬ LÝ LƯU
    @PostMapping("/create")
    public String createPlayer(@Valid @ModelAttribute PlayerDto playerDto,
                               BindingResult result,
                               Principal principal,
                               Model model,
                               RedirectAttributes redirect) {
        Team myTeam = getOwnerTeam(principal);

        if (result.hasErrors()) {
            model.addAttribute("myTeamName", myTeam.getName());
            return "player/create";
        }

        // Cưỡng chế gán vào đội của Owner (Bảo mật)
        playerDto.setTeamId(myTeam.getId());

        playerService.save(playerDto);
        redirect.addFlashAttribute("message", "Thêm cầu thủ thành công!");
        return "redirect:/admin/players";
    }

    // 4. FORM CẬP NHẬT
    @GetMapping("/update/{id}")
    public String showUpdateForm(@PathVariable Long id, Model model, Principal principal) {
        Team myTeam = getOwnerTeam(principal);
        Player player = playerService.findById(id);

        // Bảo mật: Không cho sửa cầu thủ đội khác
        if (player == null || !player.getTeam().getId().equals(myTeam.getId())) {
            return "redirect:/admin/players";
        }

        PlayerDto dto = new PlayerDto();
        BeanUtils.copyProperties(player, dto);
        dto.setTeamId(myTeam.getId());

        model.addAttribute("playerDto", dto);
        model.addAttribute("myTeamName", myTeam.getName());
        return "player/update";
    }

    // 5. XỬ LÝ CẬP NHẬT
    @PostMapping("/update")
    public String update(@Valid @ModelAttribute("playerDto") PlayerDto playerDto,
                         BindingResult bindingResult,
                         Principal principal,
                         RedirectAttributes redirect,
                         Model model) {
        Team myTeam = getOwnerTeam(principal);

        if (bindingResult.hasErrors()) {
            model.addAttribute("myTeamName", myTeam.getName());
            return "player/update";
        }

        // Cưỡng chế gán đội
        playerDto.setTeamId(myTeam.getId());

        Player existing = playerService.findById(playerDto.getId());
        if (existing != null && existing.getTeam().getId().equals(myTeam.getId())) {
            BeanUtils.copyProperties(playerDto, existing);
            existing.setTeam(myTeam); // Set lại entity team
            playerService.update(existing);
        }

        redirect.addFlashAttribute("message", "Cập nhật thành công!");
        return "redirect:/admin/players";
    }

    // 6. XÓA
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, Principal principal, RedirectAttributes redirect) {
        Team myTeam = getOwnerTeam(principal);
        Player player = playerService.findById(id);

        // Bảo mật: Chỉ xóa cầu thủ đội mình
        if (player != null && player.getTeam().getId().equals(myTeam.getId())) {
            playerService.delete(id);
            redirect.addFlashAttribute("message", "Xóa thành công!");
        } else {
            redirect.addFlashAttribute("error", "Không thể xóa cầu thủ này!");
        }
        return "redirect:/admin/players";
    }

    // Hàm phụ trợ lấy Team của Owner hiện tại
    private Team getOwnerTeam(Principal principal) {
        if (principal == null) return null;
        return accountService.findByUsername(principal.getName())
                .map(Account::getTeam)
                .orElse(null);
    }
    // 7. CHI TIẾT CẦU THỦ
    // URL: /admin/players/{id}
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) { // Dùng Integer cho an toàn
        // Gọi service tìm cầu thủ
        Player player = playerService.findById(id); // Lưu ý: tham số id của service phải khớp kiểu (Long/Integer)

        // Nếu không thấy -> Quay về danh sách
        if (player == null) {
            return "redirect:/admin/players";
        }

        model.addAttribute("player", player);
        return "player/detail"; // Trả về file templates/player/detail.html
    }
}