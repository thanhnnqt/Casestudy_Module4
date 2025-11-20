package com.example.premier_league.controller;

import com.example.premier_league.entity.Coach;
import com.example.premier_league.service.ICoachService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/coaches")
public class CoachController {

    private final ICoachService coachService;

    public CoachController(ICoachService coachService) {
        this.coachService = coachService;
    }

    @GetMapping
    public String listCoaches(@RequestParam(value = "name", required = false) String name,
                              Model model) {

        List<Coach> coaches;

        if (name != null && !name.trim().isEmpty()) {
            coaches = coachService.findByName(name.trim());
            model.addAttribute("search", name.trim());
        } else {
            coaches = coachService.findAll();
        }

        // Sắp xếp để Head Coach luôn lên đầu,
        // sau đó tới các role khác (theo alphabet), trong mỗi role sắp xếp theo tên
        Map<String, List<Coach>> coachesByRole = coaches.stream()
                .sorted((a, b) -> {
                    // Ưu tiên Head Coach
                    boolean aHead = "Head Coach".equalsIgnoreCase(a.getRole());
                    boolean bHead = "Head Coach".equalsIgnoreCase(b.getRole());

                    if (aHead && !bHead) return -1; // a trước b
                    if (!aHead && bHead) return 1;  // b trước a

                    // Nếu cùng loại (cùng role) thì sort theo tên
                    int roleCompare = a.getRole().compareToIgnoreCase(b.getRole());
                    if (roleCompare != 0) {
                        return roleCompare;
                    }
                    return a.getFullName().compareToIgnoreCase(b.getFullName());
                })
                .collect(Collectors.groupingBy(
                        Coach::getRole,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        model.addAttribute("coachesByRole", coachesByRole);
        return "coach/list"; // templates/coach/list.html
    }



    @GetMapping("/create")
    public String showCreateForm(Model model) {
        Coach coach = new Coach();
        model.addAttribute("coach", coach);
        model.addAttribute("formTitle", "Thêm huấn luyện viên");

        // Kiểm tra trong DB xem đã có Head Coach chưa
        boolean headCoachExists = coachService.existsByRole("Head Coach");
        model.addAttribute("headCoachExists", headCoachExists);

        return "coach/form";
    }


    @PostMapping("/create")
    public String createCoach(@ModelAttribute("coach") Coach coach,
                              RedirectAttributes redirectAttributes) {
        coachService.save(coach);
        redirectAttributes.addFlashAttribute("message", "Đã thêm huấn luyện viên thành công");
        return "redirect:/admin/coaches";
    }


    // 4. Form Cập nhật (GET) -> URL dạng /14/edit
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable("id") Integer id,
                               Model model,
                               RedirectAttributes redirect) {
        Coach coach = coachService.findById(id);
        if (coach == null) {
            redirect.addFlashAttribute("message", "Không tìm thấy HLV!");
            return "redirect:/admin/coaches";
        }
        model.addAttribute("coach", coach);
        model.addAttribute("formTitle", "Cập nhật huấn luyện viên");
        return "coach/form";
    }

    // 5. Xử lý Cập nhật (POST) -> Gửi về /update
    // Đây là chỗ sửa lỗi "POST not supported"
    @PostMapping("/update")
    public String updateCoach(@ModelAttribute("coach") Coach coach,
                              RedirectAttributes redirect) {
        // Khi submit form, ID đã nằm ẩn trong object coach
        coachService.save(coach);
        redirect.addFlashAttribute("message", "Cập nhật thành công!");
        return "redirect:/admin/coaches";
    }


    @GetMapping("/{id}")
    public String viewDetail(@PathVariable("id") int id,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        Coach coach = coachService.findById(id);
        if (coach == null) {
            redirectAttributes.addFlashAttribute("message", "Không tìm thấy huấn luyện viên");
            return "redirect:/admin/coaches";
        }

        model.addAttribute("coach", coach);
        return "coach/detail"; // templates/coach-detail.html
    }


    @PostMapping("/{id}/delete")
    public String deleteCoach(@PathVariable("id") int id,
                              RedirectAttributes redirectAttributes) {
        coachService.delete(id);
        redirectAttributes.addFlashAttribute("message", "Đã xoá huấn luyện viên");
        return "redirect:/admin/coaches";
    }
}
