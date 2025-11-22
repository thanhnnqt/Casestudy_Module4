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

    private void addFormAttributes(Model model) {
        model.addAttribute("teams", teamService.findAll());
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
            return "redirect:/admin/owners";
        }

        OwnerDto dto = new OwnerDto();
        BeanUtils.copyProperties(owner, dto);

        // QUAN TRỌNG: Map thủ công ID vì tên khác nhau (Entity: id -> DTO: ownerId)
        dto.setOwnerId(owner.getId());

        if (owner.getTeam() != null) dto.setTeamId(owner.getTeam().getId());
        if (owner.getAccount() != null) dto.setAccountId(owner.getAccount().getId());

        model.addAttribute("ownerDto", dto);
        addFormAttributes(model);
        return "owner/form";
    }

    // SỬA: Đổi tên biến path variable thành ownerId để tránh lỗi Identifier Altered khi xóa
    @GetMapping("/delete/{ownerId}")
    public String delete(@PathVariable("ownerId") Long ownerId, RedirectAttributes redirect) {
        try {
            ownerService.delete(ownerId);
            redirect.addFlashAttribute("message", "Đã xóa chủ sở hữu!");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Không thể xóa (có thể do ràng buộc dữ liệu).");
        }
        return "redirect:/admin/owners";
    }
}