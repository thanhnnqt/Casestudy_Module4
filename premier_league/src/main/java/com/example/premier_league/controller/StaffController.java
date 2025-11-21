package com.example.premier_league.controller;

import com.example.premier_league.dto.StaffDto;
import com.example.premier_league.entity.Account;
import com.example.premier_league.entity.Staff;
import com.example.premier_league.entity.Team;
import com.example.premier_league.service.IAccountService;
import com.example.premier_league.service.IStaffService;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/owner/staffs")
public class StaffController {

    private final IStaffService staffService;
    private final IAccountService accountService;

    @PersistenceContext
    private EntityManager entityManager;

    public StaffController(IStaffService staffService, IAccountService accountService) {
        this.staffService = staffService;
        this.accountService = accountService;
    }

    // 1. DANH SÁCH
    @GetMapping
    public String listStaffs(Model model, Principal principal) {
        Team myTeam = getOwnerTeam(principal);
        if (myTeam == null) return "redirect:/login";

        List<Staff> staffs = staffService.findByTeamId(myTeam.getId());
        Map<String, List<Staff>> staffByPosition = staffs.stream()
                .collect(Collectors.groupingBy(Staff::getPosition, LinkedHashMap::new, Collectors.toList()));

        model.addAttribute("staffByPosition", staffByPosition);
        return "staff/list";
    }

    // 2. FORM TẠO MỚI
    @GetMapping("/create")
    public String createForm(Model model, Principal principal) {
        Team myTeam = getOwnerTeam(principal);
        StaffDto staffDto = new StaffDto();
        staffDto.setTeamId(myTeam.getId());

        model.addAttribute("staffDto", staffDto);
        model.addAttribute("myTeamName", myTeam.getName());
        return "staff/create";
    }

    // 3. XỬ LÝ TẠO MỚI
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

        // Bước 1: Xóa sạch session
        entityManager.clear();

        // Bước 2: Tạo mới
        Staff staff = new Staff();
        BeanUtils.copyProperties(staffDto, staff);

        // Bước 3: Gán Team Proxy (Chỉ chứa ID)
        staff.setTeam(entityManager.getReference(Team.class, myTeam.getId()));

        staffService.save(staff);
        redirect.addFlashAttribute("message", "Thêm mới thành công!");
        return "redirect:/owner/staffs";
    }

    // 4. FORM CẬP NHẬT
    @GetMapping("/update/{id}")
    public String updateForm(@PathVariable Integer id, Model model, Principal principal) {
        Team myTeam = getOwnerTeam(principal);
        Staff staff = staffService.findById(id);

        if(staff == null || !staff.getTeam().getId().equals(myTeam.getId())) {
            return "redirect:/owner/staffs";
        }

        StaffDto staffDto = new StaffDto();
        BeanUtils.copyProperties(staff, staffDto);
        staffDto.setTeamId(myTeam.getId());

        model.addAttribute("staffDto", staffDto);
        model.addAttribute("myTeamName", myTeam.getName());
        return "staff/update";
    }

    // 5. XỬ LÝ CẬP NHẬT (FIX TRIỆT ĐỂ)
    @PostMapping("/update")
    public String updateStaff(@Valid @ModelAttribute("staffDto") StaffDto staffDto,
                              BindingResult result,
                              Principal principal,
                              RedirectAttributes redirect,
                              Model model) {
        // 1. Lấy ID đội (Lúc này session đang bị bẩn do getOwnerTeam load Account lên)
        Team dirtyTeam = getOwnerTeam(principal);
        Long teamId = dirtyTeam.getId();

        if (result.hasErrors()) {
            model.addAttribute("myTeamName", dirtyTeam.getName());
            return "staff/update";
        }

        // 2. XÓA SẠCH SESSION NGAY LẬP TỨC
        // Lệnh này làm Hibernate quên hết cái "List<Account>" đang bị lỗi kia đi
        entityManager.clear();

        // 3. Load lại Staff từ Database (Lúc này Session đang sạch)
        Staff existingStaff = staffService.findById(staffDto.getId());

        // Check quyền
        if (existingStaff == null || !existingStaff.getTeam().getId().equals(teamId)) {
            return "redirect:/owner/staffs";
        }

        // 4. Cập nhật dữ liệu
        // Copy properties TRỪ "id" và "team" và "joinDate" (để tránh null đè lên data cũ)
        BeanUtils.copyProperties(staffDto, existingStaff, "id", "team", "joinDate");

        // Nếu form có gửi joinDate thì mới set
        if (staffDto.getJoinDate() != null) {
            existingStaff.setJoinDate(staffDto.getJoinDate());
        }

        // 5. QUAN TRỌNG: Gán lại Team bằng Proxy (Chỉ chứa ID)
        // Điều này ngăn Hibernate load lại danh sách Account của Team
        existingStaff.setTeam(entityManager.getReference(Team.class, teamId));

        // 6. Lưu
        staffService.update(existingStaff);

        redirect.addFlashAttribute("message", "Cập nhật thành công!");
        return "redirect:/owner/staffs";
    }

    // 6. XỬ LÝ XÓA (FIX TRIỆT ĐỂ)
    @PostMapping("/delete/{id}")
    public String deleteStaff(@PathVariable Integer id, Principal principal, RedirectAttributes redirect) {
        // 1. Lấy ID đội
        Team dirtyTeam = getOwnerTeam(principal);
        Long teamId = dirtyTeam.getId();

        // 2. XÓA SẠCH SESSION
        entityManager.clear();

        // 3. Load lại Staff cần xóa (Session sạch)
        Staff staffToDelete = staffService.findById(id);

        // 4. Kiểm tra và xóa
        if (staffToDelete != null && staffToDelete.getTeam().getId().equals(teamId)) {
            staffService.delete(id);
            redirect.addFlashAttribute("message", "Đã xóa!");
        } else {
            redirect.addFlashAttribute("error", "Không thể xóa nhân viên này!");
        }

        return "redirect:/owner/staffs";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Integer id, Model model, Principal principal) {
        Team myTeam = getOwnerTeam(principal);
        Staff staff = staffService.findById(id);

        if (staff == null || !staff.getTeam().getId().equals(myTeam.getId())) {
            return "redirect:/owner/staffs";
        }

        model.addAttribute("staff", staff);
        return "staff/detail";
    }

    private Team getOwnerTeam(Principal principal) {
        if (principal == null) return null;
        return accountService.findByUsername(principal.getName()).map(Account::getTeam).orElse(null);
    }
}