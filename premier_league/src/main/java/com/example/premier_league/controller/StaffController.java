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
@RequestMapping("/staffs")
public class StaffController {

    private final IStaffService staffService;

    public StaffController(IStaffService staffService) {
        this.staffService = staffService;
    }

    // ==================== HIỂN THỊ DANH SÁCH ====================
    @GetMapping
    public String getAllStaff(Model model) {
        List<Staff> staffList = staffService.findAll();

        // group theo position, giữ thứ tự xuất hiện
        Map<String, List<Staff>> staffByPosition = staffList.stream()
                .collect(Collectors.groupingBy(
                        Staff::getPosition,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        model.addAttribute("staffByPosition", staffByPosition);
        return "staff/list";
    }


    // ==================== TẠO MỚI ====================
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
        return "redirect:/staffs";
    }

    // ==================== CHI TIẾT ====================
    @GetMapping("/{id}")
    public String getStaffById(@PathVariable Integer id, Model model) {
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

        model.addAttribute("staffDto", staffDto);
        return "staff/update";
    }

    @PostMapping("/update")
    public String updateStaff(@Valid @ModelAttribute("staffDto") StaffDto staffDto,
                              BindingResult bindingResult,
                              RedirectAttributes redirect,
                              Model model) {

        if (bindingResult.hasErrors()) {
            return "staff/update";
        }

        Staff existingStaff = staffService.findById(staffDto.getId());
        if (existingStaff == null) {
            model.addAttribute("message", "Không tìm thấy nhân viên với ID: " + staffDto.getId());
            return "staff/error";
        }

        BeanUtils.copyProperties(staffDto, existingStaff);
        staffService.update(existingStaff);

        redirect.addFlashAttribute("message", "Cập nhật nhân viên thành công!");
        return "redirect:/staffs";
    }

    // ==================== XÓA ====================
    @GetMapping("/delete/{id}")
    public String deleteStaff(@PathVariable Integer id, RedirectAttributes redirect) {
        Staff staff = staffService.findById(id);
        if (staff == null) {
            throw new StaffNotFoundException("Không tìm thấy nhân viên có ID: " + id);
        }

        staffService.delete(id);
        redirect.addFlashAttribute("message", "Xóa nhân viên thành công!");
        return "redirect:/staffs";
    }

//    // ==================== TÌM KIẾM ====================
//    @GetMapping("/search")
//    public String searchStaff(@RequestParam("name") String name, Model model) {
//        List<Staff> staffList = staffService.findByName(name);
//        model.addAttribute("staffList", staffList);
//        model.addAttribute("search", name);
//        return "staff/list";
//    }
}
