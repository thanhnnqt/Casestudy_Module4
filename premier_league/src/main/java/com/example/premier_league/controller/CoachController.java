package com.example.premier_league.controller;

import com.example.premier_league.entity.Account;
import com.example.premier_league.entity.Coach;
import com.example.premier_league.entity.Team;
import com.example.premier_league.service.IAccountService;
import com.example.premier_league.service.ICoachService;
import com.example.premier_league.service.ITeamService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/coaches")
public class CoachController {

    private final ICoachService coachService;
    private final IAccountService accountService;
    // Không cần ITeamService nữa vì Owner không cần load danh sách đội

    public CoachController(ICoachService coachService, IAccountService accountService) {
        this.coachService = coachService;
        this.accountService = accountService;
    }

    // 1. DANH SÁCH (Chỉ hiện HLV của đội mình)
    @GetMapping
    public String listCoaches(Model model, Principal principal) {
        Team myTeam = getOwnerTeam(principal);
        if (myTeam == null) return "redirect:/login"; // Chưa đăng nhập hoặc chưa có đội

        // Lấy danh sách HLV theo ID đội
        List<Coach> coaches = coachService.findByTeamId(myTeam.getId());

        // Grouping logic để hiển thị theo nhóm chức vụ (Giữ nguyên logic cũ của bạn)
        Map<String, List<Coach>> coachesByRole = coaches.stream()
                .collect(Collectors.groupingBy(
                        Coach::getRole,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        model.addAttribute("coachesByRole", coachesByRole);
        model.addAttribute("myTeam", myTeam); // Để hiển thị tên đội trên header nếu cần
        return "coach/list";
    }

    // 2. FORM TẠO MỚI
    @GetMapping("/create")
    public String createForm(Model model, Principal principal) {
        Team myTeam = getOwnerTeam(principal);

        Coach coach = new Coach();
        // Gán sẵn đội vào object để form hiển thị (dù readonly)
        coach.setTeam(myTeam);

        model.addAttribute("coach", coach);
        model.addAttribute("formTitle", "Thêm huấn luyện viên");
        model.addAttribute("myTeamName", myTeam.getName()); // Biến này để hiện lên ô input readonly
        return "coach/form";
    }

    // 3. XỬ LÝ TẠO MỚI
    @PostMapping("/create")
    public String createCoach(@ModelAttribute("coach") Coach coach,
                              Principal principal,
                              RedirectAttributes redirect) {
        Team myTeam = getOwnerTeam(principal);

        // Cưỡng chế gán vào đội của Owner (Bảo mật backend)
        coach.setTeam(myTeam);

        coachService.save(coach);
        redirect.addFlashAttribute("message", "Thêm mới thành công!");
        return "redirect:/admin/coaches";
    }

    // 4. FORM CẬP NHẬT
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Integer id, Model model, Principal principal) {
        Team myTeam = getOwnerTeam(principal);
        Coach coach = coachService.findById(id);

        // Bảo mật: Nếu không tìm thấy hoặc HLV này không thuộc đội của Owner -> Đá về trang danh sách
        if (coach == null || !coach.getTeam().getId().equals(myTeam.getId())) {
            return "redirect:/admin/coaches";
        }

        model.addAttribute("coach", coach);
        model.addAttribute("formTitle", "Cập nhật huấn luyện viên");
        model.addAttribute("myTeamName", myTeam.getName());
        return "coach/form";
    }

    // 5. XỬ LÝ CẬP NHẬT
    @PostMapping("/update")
    public String updateCoach(@ModelAttribute("coach") Coach coach,
                              Principal principal,
                              RedirectAttributes redirect) {
        Team myTeam = getOwnerTeam(principal);

        // Cưỡng chế gán đội (tránh trường hợp form bị hack ID đội)
        coach.setTeam(myTeam);

        coachService.save(coach);
        redirect.addFlashAttribute("message", "Cập nhật thành công!");
        return "redirect:/admin/coaches";
    }

    // 6. XÓA
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, Principal principal, RedirectAttributes redirect) {
        Team myTeam = getOwnerTeam(principal);
        Coach coach = coachService.findById(id);

        // Bảo mật: Chỉ xóa được HLV của đội mình
        if (coach != null && coach.getTeam().getId().equals(myTeam.getId())) {
            coachService.delete(id);
            redirect.addFlashAttribute("message", "Đã xóa!");
        } else {
            redirect.addFlashAttribute("error", "Không thể xóa HLV này!");
        }
        return "redirect:/admin/coaches";
    }

    // 7. CHI TIẾT
    @GetMapping("/{id}")
    public String detail(@PathVariable Integer id, Model model, Principal principal) {
        Team myTeam = getOwnerTeam(principal);
        Coach coach = coachService.findById(id);

        // Bảo mật: Chỉ xem được chi tiết HLV đội mình
        if(coach == null || !coach.getTeam().getId().equals(myTeam.getId())) {
            return "redirect:/admin/coaches";
        }

        model.addAttribute("coach", coach);
        return "coach/detail";
    }

    // --- Helper Method ---
    // Hàm này giúp lấy thông tin đội bóng của người đang đăng nhập một cách nhanh gọn
    private Team getOwnerTeam(Principal principal) {
        if (principal == null) return null;
        return accountService.findByUsername(principal.getName())
                .map(Account::getTeam)
                .orElse(null);
    }
}