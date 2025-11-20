package com.example.premier_league.controller;

import com.example.premier_league.dto.StaffDto;
import com.example.premier_league.entity.Staff;
import com.example.premier_league.exception.StaffNotFoundException;
import com.example.premier_league.service.IStaffService;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/owner/staffs")
public class StaffController {

    private final IStaffService staffService;

    public StaffController(IStaffService staffService) {
        this.staffService = staffService;
    }

    // ... (Giữ nguyên phần getAllStaff và createStaffForm) ...

    @GetMapping
    public String getAllStaff(Model model) {
        List<Staff> staffList = staffService.findAll();
        Map<String, List<Staff>> staffByPosition = staffList.stream()
                .collect(Collectors.groupingBy(
                        Staff::getPosition,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
        model.addAttribute("staffByPosition", staffByPosition);
        return "staff/list";
    }

    // ... (Giữ nguyên phần createStaff) ...
    @GetMapping("/create")
    public String createStaffForm(Model model) {
        model.addAttribute("staffDto", new StaffDto());
        return "staff/create";
    }

    @PostMapping("/create")
    public String createStaff(@Valid @ModelAttribute("staffDto") StaffDto staffDto,
                              BindingResult bindingResult,
                              RedirectAttributes redirect) {
        if (bindingResult.hasErrors()) {
            return "staff/create";
        }
        Staff staff = new Staff();
        BeanUtils.copyProperties(staffDto, staff);
        staffService.save(staff);
        redirect.addFlashAttribute("message", "Thêm mới nhân viên thành công!");
        return "redirect:/admin/staffs";
    }

    // ==================== CHI TIẾT ====================
    @GetMapping("/{id}")
    public String getStaffById(@PathVariable Integer id, Model model) {
        // Code cũ của bạn đã ổn
        Staff staff = staffService.findById(id);
        if (staff == null) {
            throw new StaffNotFoundException("Không tìm thấy nhân viên có ID: " + id);
        }
        model.addAttribute("staff", staff);
        return "staff/detail";
    }

    // ==================== CẬP NHẬT ====================
    @GetMapping("/update/{id}")
    public String showUpdateForm(@PathVariable Integer id, Model model) {
        Staff staff = staffService.findById(id);
        if (staff == null) {
            throw new StaffNotFoundException("Không tìm thấy nhân viên có ID: " + id);
        }

        StaffDto staffDto = new StaffDto();
        BeanUtils.copyProperties(staff, staffDto);

        // --- THÊM ĐOẠN NÀY ---
        // Lấy ID đội bóng hiện tại gán vào DTO để không bị lỗi Validation @NotNull
        // ---------------------

        model.addAttribute("staffDto", staffDto);
        return "staff/update";
    }

    // ==================== CẬP NHẬT NHÂN VIÊN ====================

    // HIỂN THỊ FORM CẬP NHẬT
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable("id") Integer id,
                               Model model,
                               RedirectAttributes redirectAttributes) {

        Staff staff = staffService.findById(id);
        if (staff == null) {
            redirectAttributes.addFlashAttribute("message", "Không tìm thấy nhân viên");
            return "redirect:/admin/staffs";
        }

        StaffDto staffDto = new StaffDto();
        // copy từ entity sang dto để bind lên form
        BeanUtils.copyProperties(staff, staffDto);

        model.addAttribute("staffDto", staffDto);
        model.addAttribute("formTitle", "Cập nhật nhân viên");
        return "staff/update";   // trang form cập nhật nhân viên
    }

    // XỬ LÝ SUBMIT FORM CẬP NHẬT
    @PostMapping("/{id}/edit")
    public String updateStaff(@PathVariable("id") Integer id,
                              @Valid @ModelAttribute("staffDto") StaffDto staffDto,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes,
                              Model model) {

        if (bindingResult.hasErrors()) {
            // Có lỗi validate → quay lại form
            model.addAttribute("formTitle", "Cập nhật nhân viên");
            return "staff/update";
        }

        // Tìm nhân viên cũ theo id trên URL (không phụ thuộc vào staffDto.getId())
        Staff existingStaff = staffService.findById(id);
        if (existingStaff == null) {
            throw new StaffNotFoundException("Không tìm thấy nhân viên để cập nhật");
        }

        // Cập nhật THỦ CÔNG từ DTO sang entity (an toàn)
        existingStaff.setFullName(staffDto.getFullName());
        existingStaff.setDateOfBirth(staffDto.getDateOfBirth());
        existingStaff.setGender(staffDto.getGender());
        existingStaff.setNationality(staffDto.getNationality());
        existingStaff.setPosition(staffDto.getPosition());
        existingStaff.setRole(staffDto.getRole());
        existingStaff.setJoinDate(staffDto.getJoinDate());
        existingStaff.setPhoneNumber(staffDto.getPhoneNumber());
        existingStaff.setEmail(staffDto.getEmail());
        existingStaff.setStatus(staffDto.getStatus());

        // Nếu có nhập avatar mới thì update, không thì giữ ảnh cũ
        if (staffDto.getAvatarUrl() != null && !staffDto.getAvatarUrl().isEmpty()) {
            existingStaff.setAvatarUrl(staffDto.getAvatarUrl());
        }

        // TODO: nếu có teamId trong DTO thì set team ở đây

        staffService.update(existingStaff);

        redirectAttributes.addFlashAttribute("message", "Cập nhật nhân viên thành công!");
        return "redirect:/admin/staffs";
    }


    // ==================== XÓA (Đã sửa) ====================
    // Đổi từ @GetMapping sang @PostMapping để bảo mật hơn
    @PostMapping("/delete/{id}")
    public String deleteStaff(@PathVariable Integer id, RedirectAttributes redirect) {
        Staff staff = staffService.findById(id);
        if (staff == null) {
            throw new StaffNotFoundException("Không tìm thấy nhân viên có ID: " + id);
        }

        staffService.delete(id);
        redirect.addFlashAttribute("message", "Xóa nhân viên thành công!");
        return "redirect:/admin/staffs";
    }
}