package com.example.premier_league.controller;

import com.example.premier_league.entity.Account;
import com.example.premier_league.entity.Coach;
import com.example.premier_league.entity.Team;
import com.example.premier_league.service.IAccountService;
import com.example.premier_league.service.ICoachService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/owner/coaches")
public class CoachController {

    private final ICoachService coachService;
    private final IAccountService accountService;

    // 1. BẮT BUỘC PHẢI CÓ CÁI NÀY ĐỂ FIX LỖI
    @PersistenceContext
    private EntityManager entityManager;

    public CoachController(ICoachService coachService, IAccountService accountService) {
        this.coachService = coachService;
        this.accountService = accountService;
    }

    // 1. DANH SÁCH
    @GetMapping
    public String listCoaches(Model model, Principal principal) {
        Team myTeam = getOwnerTeam(principal);
        if (myTeam == null) return "redirect:/login";

        List<Coach> coaches = coachService.findByTeamId(myTeam.getId());

        // 1. TẠO COMPARATOR ĐỂ SẮP XẾP KEY
        // Logic: Nếu là "Head Coach" thì cho lên đầu (-1), còn lại sắp xếp Alpha (A-Z)
        Comparator<String> roleComparator = (role1, role2) -> {
            if ("Head Coach".equalsIgnoreCase(role1)) return -1; // Ưu tiên số 1
            if ("Head Coach".equalsIgnoreCase(role2)) return 1;
            return role1.compareTo(role2); // Các role khác xếp theo thứ tự ABC
        };

        // 2. GOM NHÓM VÀ SẮP XẾP
        Map<String, List<Coach>> coachesByRole = coaches.stream()
                .collect(Collectors.groupingBy(
                        Coach::getRole,
                        // Thay LinkedHashMap bằng TreeMap có chứa Comparator ở trên
                        () -> new TreeMap<>(roleComparator),
                        Collectors.toList()
                ));

        model.addAttribute("coachesByRole", coachesByRole);
        model.addAttribute("myTeam", myTeam);
        return "coach/list";
    }

    // 2. FORM TẠO MỚI
    @GetMapping("/create")
    public String createForm(Model model, Principal principal) {
        Team myTeam = getOwnerTeam(principal);
        Coach coach = new Coach();
        // Chỉ để hiển thị tên đội trên form (Read-only)
        coach.setTeam(myTeam);

        boolean headCoachExists = checkHeadCoachExists(myTeam.getId());
        model.addAttribute("headCoachExists", headCoachExists);
        model.addAttribute("coach", coach);
        model.addAttribute("formTitle", "Thêm huấn luyện viên");
        model.addAttribute("myTeamName", myTeam.getName());
        return "coach/form";
    }

    // 3. XỬ LÝ TẠO MỚI (FIX LỖI BẰNG CLEAR SESSION)
    @PostMapping("/create")
    public String createCoach(@ModelAttribute("coach") Coach coachForm,
                              Principal principal,
                              RedirectAttributes redirect) {
        // Lấy thông tin Team ID hiện tại
        Team myTeam = getOwnerTeam(principal);
        Long teamId = myTeam.getId();

        // Check logic HLV trưởng
        if ("Head Coach".equals(coachForm.getRole()) && checkHeadCoachExists(teamId)) {
            redirect.addFlashAttribute("error", "Đội bóng đã có HLV trưởng!");
            return "redirect:/owner/coaches";
        }

        // --- QUAN TRỌNG NHẤT: XÓA SẠCH BỘ NHỚ HIBERNATE ---
        // Lệnh này ngắt đứt mọi liên kết Account-Team đang gây lỗi
        entityManager.clear();
        // --------------------------------------------------

        // Tạo HLV mới sạch sẽ
        Coach newCoach = new Coach();
        BeanUtils.copyProperties(coachForm, newCoach, "id", "team");

        // Dùng getReference để lấy Team giả (chỉ chứa ID), không load Account
        newCoach.setTeam(entityManager.getReference(Team.class, teamId));

        coachService.save(newCoach);
        redirect.addFlashAttribute("message", "Thêm mới thành công!");
        return "redirect:/owner/coaches";
    }

    // 4. FORM CẬP NHẬT
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Integer id, Model model, Principal principal) {
        Team myTeam = getOwnerTeam(principal);
        Coach coach = coachService.findById(id);

        if (coach == null || !coach.getTeam().getId().equals(myTeam.getId())) {
            return "redirect:/owner/coaches";
        }

        boolean headCoachExists = checkHeadCoachExists(myTeam.getId());
        model.addAttribute("headCoachExists", headCoachExists);
        model.addAttribute("coach", coach);
        model.addAttribute("formTitle", "Cập nhật huấn luyện viên");
        model.addAttribute("myTeamName", myTeam.getName());
        return "coach/form";
    }

    // 5. XỬ LÝ CẬP NHẬT (FIX LỖI BẰNG CLEAR SESSION)
    @PostMapping("/update")
    public String updateCoach(@ModelAttribute("coach") Coach coachForm,
                              Principal principal,
                              RedirectAttributes redirect) {
        Team myTeam = getOwnerTeam(principal);
        Long teamId = myTeam.getId();

        // Check logic HLV trưởng
        if ("Head Coach".equals(coachForm.getRole())) {
            List<Coach> teamCoaches = coachService.findByTeamId(teamId);
            boolean exists = teamCoaches.stream()
                    .anyMatch(c -> "Head Coach".equals(c.getRole()) && !c.getId().equals(coachForm.getId()));

            if (exists) {
                redirect.addFlashAttribute("error", "Đội bóng đã có HLV trưởng!");
                return "redirect:/owner/coaches";
            }
        }

        // Kiểm tra HLV có thuộc team không trước khi clear
        Coach existingCheck = coachService.findById(coachForm.getId());
        if (existingCheck == null || !existingCheck.getTeam().getId().equals(teamId)) {
            return "redirect:/owner/coaches";
        }

        // --- QUAN TRỌNG NHẤT: XÓA SẠCH BỘ NHỚ HIBERNATE ---
        entityManager.clear();
        // --------------------------------------------------

        // Set lại Team ID chuẩn vào form gửi lên
        coachForm.setTeam(entityManager.getReference(Team.class, teamId));

        coachService.save(coachForm); // Lưu thẳng
        redirect.addFlashAttribute("message", "Cập nhật thành công!");
        return "redirect:/owner/coaches";
    }

    // 6. XÓA (FIX LỖI BẰNG CLEAR SESSION)
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, Principal principal, RedirectAttributes redirect) {
        Team myTeam = getOwnerTeam(principal);
        Long teamId = myTeam.getId();

        Coach coach = coachService.findById(id);

        if (coach != null && coach.getTeam().getId().equals(teamId)) {

            // --- QUAN TRỌNG NHẤT: XÓA SẠCH BỘ NHỚ TRƯỚC KHI XÓA ---
            entityManager.clear();
            // ------------------------------------------------------

            coachService.delete(id);
            redirect.addFlashAttribute("message", "Đã xóa!");
        } else {
            redirect.addFlashAttribute("error", "Không thể xóa HLV này!");
        }
        return "redirect:/owner/coaches";
    }

    // 7. CHI TIẾT
    @GetMapping("/{id}")
    public String detail(@PathVariable Integer id, Model model, Principal principal) {
        Team myTeam = getOwnerTeam(principal);
        Coach coach = coachService.findById(id);

        if(coach == null || !coach.getTeam().getId().equals(myTeam.getId())) {
            return "redirect:/owner/coaches";
        }
        model.addAttribute("coach", coach);
        return "coach/detail";
    }

    private Team getOwnerTeam(Principal principal) {
        if (principal == null) return null;
        return accountService.findByUsername(principal.getName())
                .map(Account::getTeam)
                .orElse(null);
    }

    private boolean checkHeadCoachExists(Long teamId) {
        List<Coach> coaches = coachService.findByTeamId(teamId);
        if (coaches == null || coaches.isEmpty()) {
            return false;
        }
        return coaches.stream()
                .anyMatch(c -> "Head Coach".equalsIgnoreCase(c.getRole()));
    }
}