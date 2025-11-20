package com.example.premier_league.controller;

import com.example.premier_league.dto.TournamentDto;
import com.example.premier_league.entity.Team;
import com.example.premier_league.entity.Tournament;
import com.example.premier_league.service.ITeamService;
import com.example.premier_league.service.ITournamentService;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class TournamentController {
    private final ITournamentService tournamentService;
    private final ITeamService teamService;
    public TournamentController(ITournamentService tournamentService, ITeamService teamService) {
        this.tournamentService = tournamentService;
        this.teamService = teamService;
    }


    @GetMapping("/tournaments")
    public String list(Model model) {
        List<Tournament> list = tournamentService.findAll();

        // --- DEBUG ---
        System.out.println("========================================");
        System.out.println("Đang truy cập trang List Tournament");
        System.out.println("Số lượng bản ghi tìm thấy: " + list.size());
        for (Tournament t : list) {
            System.out.println(" - ID: " + t.getId() + ", Name: " + t.getName());
        }
        System.out.println("========================================");
        // -------------

        model.addAttribute("tournaments", list);
        return "tournament/list";
    }

    @GetMapping("/tournaments/add")
    public String addForm(Model model) {
        // Sửa thành "tournamentDto" để khớp với th:object trong HTML
        model.addAttribute("tournamentDto", new TournamentDto());
        return "tournament/add";
    }


    @PostMapping("/tournaments/add")
    public String save(@Valid @ModelAttribute("tournamentDto") TournamentDto tournamentDto,
                       BindingResult bindingResult,
                       Model model) {

        // --- DEBUG START ---
        System.out.println("--- ĐANG THỰC HIỆN SAVE GIẢI ĐẤU ---");
        System.out.println("Mùa giải nhận được: " + tournamentDto.getSeason());
        System.out.println("Ngày bắt đầu: " + tournamentDto.getStartDate());
        System.out.println("Ngày kết thúc: " + tournamentDto.getEndDate());
        // --- DEBUG END ---

        // Validate ngày tháng (giữ nguyên code cũ)
        if (tournamentDto.getStartDate() != null && tournamentDto.getEndDate() != null) {
            if (!tournamentDto.getEndDate().isAfter(tournamentDto.getStartDate())) {
                bindingResult.rejectValue("endDate", "error.tournamentDto", "Ngày kết thúc phải sau ngày bắt đầu!");
            }
        }
        // 2. Validate Logic: KIỂM TRA TRÙNG MÙA GIẢI (MỚI)
        // Chỉ kiểm tra khi thêm mới (ID == null)
        if (tournamentDto.getId() == null && tournamentService.existsBySeason(tournamentDto.getSeason())) {
            // rejectValue(tên_trường, mã_lỗi, thông_báo)
            bindingResult.rejectValue("season", "error.tournamentDto", "Mùa giải " + tournamentDto.getSeason() + " đã tồn tại trong hệ thống!");
        }
        // Kiểm tra lỗi Validation
        if (bindingResult.hasErrors()) {
            // --- DEBUG START ---
            System.out.println("!!! CÓ LỖI VALIDATION !!!");
            bindingResult.getAllErrors().forEach(e -> System.out.println(e.getDefaultMessage()));
            // --- DEBUG END ---

            return "tournament/add"; // Trả về lại trang thêm mới để hiện lỗi
        }

        // Chuyển đổi và Lưu
        try {
            Tournament tournament = new Tournament();
            BeanUtils.copyProperties(tournamentDto, tournament);
            tournament.setName("Premier League");

            // --- DEBUG START ---
            System.out.println("Đang gọi lệnh save xuống DB...");
            // --- DEBUG END ---

            tournamentService.save(tournament);

            // --- DEBUG START ---
            System.out.println("Lưu thành công! Đang chuyển hướng về danh sách.");
            // --- DEBUG END ---

            return "redirect:/tournaments";

        } catch (Exception e) {
            e.printStackTrace(); // In lỗi ra console nếu có lỗi DB
            model.addAttribute("error", "Lỗi hệ thống: " + e.getMessage());
            return "tournament/add";
        }
    }

    @GetMapping("/tournaments/delete/{id}")
    public String delete(@PathVariable Long id) {
        tournamentService.delete(id);
        return "redirect:/tournaments";
    }

    @GetMapping("/tournaments/{id}/manage-teams")
    public String showManageTeamsForm(@PathVariable Long id, Model model) {
        Tournament tournament = tournamentService.findById(id);
        if (tournament == null) return "redirect:/tournaments";

        List<Team> allTeams = teamService.findAll();

        // Lấy danh sách các giải đấu KHÁC (để làm chức năng copy)
        List<Tournament> otherTournaments = tournamentService.findAll().stream()
                .filter(t -> !t.getId().equals(id))
                .collect(Collectors.toList());

        // Kiểm tra trạng thái giải đấu
        boolean isLocked = tournamentService.isTournamentStarted(id);

        model.addAttribute("tournament", tournament);
        model.addAttribute("allTeams", allTeams);
        model.addAttribute("otherTournaments", otherTournaments);
        model.addAttribute("isLocked", isLocked); // Biến này để disable form bên View

        // Danh sách ID đã chọn
        model.addAttribute("tournamentTeamIds",
                tournament.getTeams().stream().map(Team::getId).collect(Collectors.toSet())
        );

        return "tournament/manage-teams";
    }

    @PostMapping("/tournaments/save-teams")
    public String saveTeamsForTournament(@RequestParam("tournamentId") Long tournamentId,
                                         @RequestParam(value = "teamIds", required = false) List<Long> teamIds,
                                         RedirectAttributes redirect) {
        try {
            // 1. Validate số lượng đội
            if (teamIds == null || teamIds.size() != 20) {
                redirect.addFlashAttribute("error", "Lỗi: Giải đấu Premier League bắt buộc phải có đúng 20 đội bóng! Bạn đang chọn: " + (teamIds == null ? 0 : teamIds.size()));
                return "redirect:/tournaments/" + tournamentId + "/manage-teams";
            }

            // 2. Thực hiện lưu
            tournamentService.updateTeamsForTournament(tournamentId, teamIds);
            redirect.addFlashAttribute("message", "Cập nhật danh sách 20 đội thành công!");

        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/tournaments/" + tournamentId + "/manage-teams";
    }

    // --- CHỨC NĂNG MỚI: COPY TỪ GIẢI CŨ ---
    @PostMapping("/tournaments/copy-teams")
    public String copyTeams(@RequestParam("targetId") Long targetId,
                            @RequestParam("sourceId") Long sourceId,
                            RedirectAttributes redirect) {
        try {
            tournamentService.copyTeamsFromTournament(targetId, sourceId);
            redirect.addFlashAttribute("message", "Đã sao chép danh sách đội thành công!");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Lỗi sao chép: " + e.getMessage());
        }
        return "redirect:/tournaments/" + targetId + "/manage-teams";
    }
}
