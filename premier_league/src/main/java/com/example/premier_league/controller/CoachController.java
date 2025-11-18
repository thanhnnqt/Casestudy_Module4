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
@RequestMapping("/coaches")
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

        Map<String, List<Coach>> coachesByRole = coaches.stream()
                .collect(Collectors.groupingBy(Coach::getRole,
                        LinkedHashMap::new,
                        Collectors.toList()));

        model.addAttribute("coachesByRole", coachesByRole);
        return "coach/list"; // templates/coaches.html
    }


    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("coach", new Coach());
        model.addAttribute("formTitle", "Thêm huấn luyện viên");
        return "coach/form"; // templates/coach-form.html
    }

    @PostMapping("/create")
    public String createCoach(@ModelAttribute("coach") Coach coach,
                              RedirectAttributes redirectAttributes) {
        coachService.save(coach);
        redirectAttributes.addFlashAttribute("message", "Đã thêm huấn luyện viên thành công");
        return "redirect:/coaches";
    }


    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable("id") int id,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        Coach coach = coachService.findById(id);
        if (coach == null) {
            redirectAttributes.addFlashAttribute("message", "Không tìm thấy huấn luyện viên");
            return "redirect:/coaches";
        }

        model.addAttribute("coach", coach);
        model.addAttribute("formTitle", "Cập nhật huấn luyện viên");
        return "coach/form";
    }

    @PostMapping("/{id}/edit")
    public String updateCoach(@PathVariable("id") Integer id,
                              @ModelAttribute("coach") Coach coach,
                              RedirectAttributes redirectAttributes) {

        coach.setId(id);
        coachService.update(coach);

        redirectAttributes.addFlashAttribute("message", "Đã cập nhật huấn luyện viên thành công");
        return "redirect:/coaches";
    }


    @GetMapping("/{id}")
    public String viewDetail(@PathVariable("id") int id,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        Coach coach = coachService.findById(id);
        if (coach == null) {
            redirectAttributes.addFlashAttribute("message", "Không tìm thấy huấn luyện viên");
            return "redirect:/coaches";
        }

        model.addAttribute("coach", coach);
        return "coach/detail"; // templates/coach-detail.html
    }


    @PostMapping("/{id}/delete")
    public String deleteCoach(@PathVariable("id") int id,
                              RedirectAttributes redirectAttributes) {
        coachService.delete(id);
        redirectAttributes.addFlashAttribute("message", "Đã xoá huấn luyện viên");
        return "redirect:/coaches";
    }
}
