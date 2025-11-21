package com.example.premier_league.controller;

import com.example.premier_league.dto.StaffDto;
import com.example.premier_league.entity.Account;
import com.example.premier_league.entity.Staff;
import com.example.premier_league.entity.Team;
import com.example.premier_league.service.IAccountService;
import com.example.premier_league.service.IStaffService;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/owner/staffs")
public class StaffController {

    private final IStaffService staffService;
    private final IAccountService accountService;

    public StaffController(IStaffService staffService, IAccountService accountService) {
        this.staffService = staffService;
        this.accountService = accountService;
    }

    @GetMapping
    public String listStaffs(Model model, Principal principal) {
        Team myTeam = getOwnerTeam(principal);
        List<Staff> staffs = staffService.findByTeamId(myTeam.getId());

        Map<String, List<Staff>> staffByPosition = staffs.stream()
                .collect(Collectors.groupingBy(Staff::getPosition, LinkedHashMap::new, Collectors.toList()));

        model.addAttribute("staffByPosition", staffByPosition);
        return "staff/list";
    }

    @GetMapping("/create")
    public String createForm(Model model, Principal principal) {
        Team myTeam = getOwnerTeam(principal);
        StaffDto staffDto = new StaffDto();
        staffDto.setTeamId(myTeam.getId());

        model.addAttribute("staffDto", staffDto);
        model.addAttribute("myTeamName", myTeam.getName());
        return "staff/create";
    }

    @PostMapping("/create")
    public String createStaff(@Valid @ModelAttribute("staffDto") StaffDto staffDto,
                              BindingResult result,
                              Principal principal,
                              RedirectAttributes redirect,
                              Model model) {
        Team myTeam = getOwnerTeam(principal);
        if (result.hasErrors()) {
            model.addAttribute("myTeamName", myTeam.getName());
            return "staff/create";
        }

        staffDto.setTeamId(myTeam.getId());
        Staff staff = new Staff();
        BeanUtils.copyProperties(staffDto, staff);
        staff.setTeam(myTeam);
        staffService.save(staff);

        redirect.addFlashAttribute("message", "Thêm mới thành công!");
        return "redirect:/admin/staffs";
    }

    @GetMapping("/update/{id}")
    public String updateForm(@PathVariable Integer id, Model model, Principal principal) {
        Team myTeam = getOwnerTeam(principal);
        Staff staff = staffService.findById(id);

        if(staff == null || !staff.getTeam().getId().equals(myTeam.getId())) return "redirect:/admin/staffs";

        StaffDto staffDto = new StaffDto();
        BeanUtils.copyProperties(staff, staffDto);
        staffDto.setTeamId(myTeam.getId());

        model.addAttribute("staffDto", staffDto);
        model.addAttribute("myTeamName", myTeam.getName());
        return "staff/update";
    }

    @PostMapping("/update")
    public String updateStaff(@Valid @ModelAttribute("staffDto") StaffDto staffDto,
                              BindingResult result,
                              Principal principal,
                              RedirectAttributes redirect,
                              Model model) {
        Team myTeam = getOwnerTeam(principal);
        if (result.hasErrors()) {
            model.addAttribute("myTeamName", myTeam.getName());
            return "staff/update";
        }

        staffDto.setTeamId(myTeam.getId());
        Staff existing = staffService.findById(staffDto.getId());
        if (existing != null && existing.getTeam().getId().equals(myTeam.getId())) {
            BeanUtils.copyProperties(staffDto, existing);
            existing.setTeam(myTeam);
            staffService.update(existing);
        }

        redirect.addFlashAttribute("message", "Cập nhật thành công!");
        return "redirect:/admin/staffs";
    }

    @PostMapping("/delete/{id}")
    public String deleteStaff(@PathVariable Integer id, Principal principal, RedirectAttributes redirect) {
        Team myTeam = getOwnerTeam(principal);
        Staff staff = staffService.findById(id);
        if (staff != null && staff.getTeam().getId().equals(myTeam.getId())) {
            staffService.delete(id);
            redirect.addFlashAttribute("message", "Đã xóa!");
        }
        return "redirect:/admin/staffs";
    }

    private Team getOwnerTeam(Principal principal) {
        if (principal == null) return null;
        return accountService.findByUsername(principal.getName()).map(Account::getTeam).orElse(null);
    }

    // ... Các hàm create, update, delete cũ giữ nguyên ...

    // --- BỔ SUNG HÀM NÀY ĐỂ SỬA LỖI ---
    // URL: /admin/staffs/{id}
    @GetMapping("/{id}")
    public String detail(@PathVariable Integer id, Model model, Principal principal) {
        // 1. Lấy đội bóng của Owner đang đăng nhập
        Team myTeam = getOwnerTeam(principal);

        // 2. Tìm nhân viên theo ID
        Staff staff = staffService.findById(id);

        // 3. Kiểm tra BẢO MẬT:
        // - Nếu không tìm thấy nhân viên
        // - HOẶC nhân viên này không thuộc đội của Owner (tránh soi dữ liệu đội khác)
        if (staff == null || !staff.getTeam().getId().equals(myTeam.getId())) {
            return "redirect:/admin/staffs";
        }

        model.addAttribute("staff", staff);
        return "staff/detail"; // Trả về file detail.html
    }

    // ... (Hàm getOwnerTeam ở cuối file giữ nguyên) ...
}