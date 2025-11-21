package com.example.premier_league.controller;

import com.example.premier_league.dto.OwnerDto;
import com.example.premier_league.entity.Owner;
import com.example.premier_league.service.IAccountService;
import com.example.premier_league.service.IOwnerService;
import com.example.premier_league.service.ITeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/owners")
@RequiredArgsConstructor
public class OwnerController {

    private final IOwnerService ownerService;
    private final ITeamService teamService;
    private final IAccountService accountService;

    // Helper để load danh sách cho dropdown
    private void addFormAttributes(Model model) {
        model.addAttribute("teams", teamService.findAll());
        // Trong thực tế, bạn có thể cần lọc chỉ lấy những account chưa có chủ sở hữu
        model.addAttribute("accounts", accountService.findAll());
    }

    @GetMapping
    public String showList(Model model) {
        model.addAttribute("owners", ownerService.findAll());
        return "owner/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("ownerDto", new OwnerDto());
        addFormAttributes(model);
        return "owner/form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("ownerDto") OwnerDto ownerDto,
                       BindingResult bindingResult,
                       Model model,
                       RedirectAttributes redirect) {

        if (bindingResult.hasErrors()) {
            addFormAttributes(model);
            return "owner/form";
        }

        try {
            ownerService.saveFromDto(ownerDto);
            redirect.addFlashAttribute("message", "Lưu thông tin chủ sở hữu thành công!");
        } catch (Exception e) {
            // Xử lý lỗi (ví dụ: trùng đội bóng)
            model.addAttribute("error", "Lỗi: " + e.getMessage());
            addFormAttributes(model);
            return "owner/form";
        }

        return "redirect:/admin/owners";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirect) {
        Owner owner = ownerService.findById(id);
        if (owner == null) {
            redirect.addFlashAttribute("error", "Không tìm thấy dữ liệu!");
            return "redirect:/owners";
        }

        // Map Entity sang DTO
        OwnerDto dto = new OwnerDto();
        BeanUtils.copyProperties(owner, dto);

        if (owner.getTeam() != null) dto.setTeamId(owner.getTeam().getId());
        if (owner.getAccount() != null) dto.setAccountId(owner.getAccount().getId());

        model.addAttribute("ownerDto", dto);
        addFormAttributes(model);
        return "owner/form";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            ownerService.delete(id);
            redirect.addFlashAttribute("message", "Đã xóa chủ sở hữu!");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Không thể xóa (có thể do ràng buộc dữ liệu).");
        }
        return "redirect:/admin/owners";
    }
}