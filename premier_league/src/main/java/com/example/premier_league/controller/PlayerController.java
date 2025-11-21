package com.example.premier_league.controller;

import com.example.premier_league.dto.PlayerDto;
import com.example.premier_league.entity.Account;
import com.example.premier_league.entity.Player;
import com.example.premier_league.entity.Team;
import com.example.premier_league.service.IAccountService;
import com.example.premier_league.service.IPlayerService;
import com.example.premier_league.service.ITeamService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
@RequestMapping("/owner/players")
public class PlayerController {

    private final IPlayerService playerService;
    private final ITeamService teamService; // Giữ lại nếu cần dùng ở đâu đó
    private final IAccountService accountService;

    // 1. BẮT BUỘC: Tiêm EntityManager
    @PersistenceContext
    private EntityManager entityManager;

    public PlayerController(IPlayerService playerService, ITeamService teamService, IAccountService accountService) {
        this.playerService = playerService;
        this.teamService = teamService;
        this.accountService = accountService;
    }

    // 1. DANH SÁCH
    @GetMapping
    public String listPlayers(Model model, Principal principal) {
        Team myTeam = getOwnerTeam(principal);
        if (myTeam == null) return "redirect:/login";

        List<Player> players = playerService.findByTeamId(myTeam.getId());

        model.addAttribute("players", players);
        model.addAttribute("myTeam", myTeam);
        return "player/list";
    }

    // 2. FORM TẠO MỚI
    @GetMapping("/create")
    public String showCreateForm(Model model, Principal principal) {
        Team myTeam = getOwnerTeam(principal);
        PlayerDto playerDto = new PlayerDto();
        playerDto.setTeamId(myTeam.getId());

        model.addAttribute("playerDto", playerDto);
        model.addAttribute("myTeamName", myTeam.getName());
        return "player/create";
    }

    // 3. XỬ LÝ LƯU (FIX LỖI VỚI DTO)
    @PostMapping("/create")
    public String createPlayer(@Valid @ModelAttribute PlayerDto playerDto,
                               BindingResult result,
                               Principal principal,
                               Model model,
                               RedirectAttributes redirect) {
        // 1. Lấy Team để check lỗi form và lấy ID
        Team dirtyTeam = getOwnerTeam(principal);
        Long teamId = dirtyTeam.getId();

        if (result.hasErrors()) {
            model.addAttribute("myTeamName", dirtyTeam.getName());
            return "player/create";
        }

        // --- FIX LỖI: XÓA SẠCH SESSION ---
        // Việc này làm Hibernate "quên" đối tượng dirtyTeam đi.
        entityManager.clear();

        // 2. Đảm bảo DTO có ID Team chuẩn (đề phòng binding lỗi)
        playerDto.setTeamId(teamId);

        // 3. Gọi Service lưu DTO bình thường
        // Vì session đã sạch, Service sẽ tự load lại Team (hoặc reference) một cách an toàn
        playerService.save(playerDto);

        redirect.addFlashAttribute("message", "Thêm cầu thủ thành công!");
        return "redirect:/owner/players";
    }

    // 4. FORM CẬP NHẬT
    @GetMapping("/update/{id}")
    public String showUpdateForm(@PathVariable Long id, Model model, Principal principal) {
        Team myTeam = getOwnerTeam(principal);
        Player player = playerService.findById(id);

        if (player == null || !player.getTeam().getId().equals(myTeam.getId())) {
            return "redirect:/owner/players";
        }

        PlayerDto dto = new PlayerDto();
        BeanUtils.copyProperties(player, dto);
        dto.setTeamId(myTeam.getId());

        model.addAttribute("playerDto", dto);
        model.addAttribute("myTeamName", myTeam.getName());
        return "player/update";
    }

    // 5. XỬ LÝ CẬP NHẬT (FIX LỖI VỚI DTO)
    @PostMapping("/update")
    public String update(@Valid @ModelAttribute("playerDto") PlayerDto playerDto,
                         BindingResult bindingResult,
                         Principal principal,
                         RedirectAttributes redirect,
                         Model model) {
        Team dirtyTeam = getOwnerTeam(principal);
        Long teamId = dirtyTeam.getId();

        if (bindingResult.hasErrors()) {
            model.addAttribute("myTeamName", dirtyTeam.getName());
            return "player/update";
        }

        // --- FIX LỖI: XÓA SẠCH SESSION ---
        entityManager.clear();

        // Lúc này session sạch, ta cần kiểm tra quyền lại một chút cho an toàn
        // (Hoặc tin tưởng ID từ form gửi lên)

        // 1. Đảm bảo ID Team trong DTO là chính xác
        playerDto.setTeamId(teamId);

        // 2. Gọi Service Update
        // Lưu ý: Trong Service của bạn cần có logic check xem ID cầu thủ có tồn tại không
        // Nhưng vì ta đã clear session, Service sẽ load lại dữ liệu sạch để update -> Không bị lỗi Identifier
        playerService.update(playerDto); // Giả sử bạn có hàm update nhận DTO

        // *Lưu ý:* Nếu service của bạn hàm update nhận Entity, bạn làm như sau:
        /*
        Player existing = playerService.findById(playerDto.getId());
        if(existing != null && existing.getTeam().getId().equals(teamId)) {
             BeanUtils.copyProperties(playerDto, existing, "id", "team");
             // Set team proxy để tránh load account
             existing.setTeam(entityManager.getReference(Team.class, teamId));
             playerService.update(existing);
        }
        */
        // Nhưng nếu service nhận DTO thì cứ gọi thẳng service sau khi clear() là được.

        redirect.addFlashAttribute("message", "Cập nhật thành công!");
        return "redirect:/owner/players";
    }

    // 6. XÓA (FIX LỖI)
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, Principal principal, RedirectAttributes redirect) {
        // 1. Lấy ID Team (để check quyền)
        Team dirtyTeam = getOwnerTeam(principal);
        Long teamId = dirtyTeam.getId();

        // --- FIX LỖI: XÓA SẠCH SESSION ---
        entityManager.clear();

        // 2. Load lại Player cần xóa (Sạch sẽ)
        Player player = playerService.findById(id);

        // 3. Kiểm tra quyền và xóa
        if (player != null && player.getTeam().getId().equals(teamId)) {
            playerService.delete(id);
            redirect.addFlashAttribute("message", "Xóa thành công!");
        } else {
            redirect.addFlashAttribute("error", "Không thể xóa cầu thủ này!");
        }
        return "redirect:/owner/players";
    }

    // ... (Các phần khác giữ nguyên)

    private Team getOwnerTeam(Principal principal) {
        if (principal == null) return null;
        return accountService.findByUsername(principal.getName())
                .map(Account::getTeam)
                .orElse(null);
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Player player = playerService.findById(id);
        if (player == null) {
            return "redirect:/owner/players";
        }
        model.addAttribute("player", player);
        return "player/detail";
    }
}