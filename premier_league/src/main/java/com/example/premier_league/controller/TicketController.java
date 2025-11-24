package com.example.premier_league.controller;

import com.example.premier_league.entity.MatchSchedule;
import com.example.premier_league.entity.Stadium;
import com.example.premier_league.entity.Team;
import com.example.premier_league.entity.Ticket;
import com.example.premier_league.service.IMatchScheduleService;
import com.example.premier_league.service.ITeamService;
import com.example.premier_league.service.impl.IStadiumService;
import com.example.premier_league.service.impl.ITicketService;
import com.example.premier_league.service.impl.ITicketTypeService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping("/owner/tickets/{teamId}")
public class TicketController {

    private final ITicketService ticketService;
    private final ITicketTypeService ticketTypeService;
    private final IMatchScheduleService matchScheduleService;
    private final IStadiumService stadiumService;
    private final ITeamService teamService;

    public TicketController(ITicketService ticketService,
                            ITicketTypeService ticketTypeService,
                            IMatchScheduleService matchScheduleService,
                            IStadiumService stadiumService,
                            ITeamService teamService) {
        this.ticketService = ticketService;
        this.ticketTypeService = ticketTypeService;
        this.matchScheduleService = matchScheduleService;
        this.stadiumService = stadiumService;
        this.teamService = teamService;
    }

    /**
     * Hiển thị danh sách vé của CLB (theo teamId)
     */
    @GetMapping
    public String showTicketList(Model model,
                                 @PathVariable("teamId") Long teamId) {

        Team team = teamService.findById(teamId);
        if (team == null) {
            model.addAttribute("mess", "Không tìm thấy câu lạc bộ.");
            return "ticket/list";
        }

        // Lấy tất cả vé mà đội này là đội chủ nhà
        List<Ticket> ticketList = ticketService.findAllByHomeTeam(team.getName());

        if (ticketList == null || ticketList.isEmpty()) {
            model.addAttribute("mess", "Hiện chưa có vé nào được tạo cho " + team.getName() + ".");
        } else {
            model.addAttribute("ticketList", ticketList);
        }

        // để view còn build lại URL /owner/tickets/{teamId}/...
        model.addAttribute("teamId", teamId);

        return "ticket/list";
    }

    /**
     * Form tạo vé cho một trận cụ thể
     */
    @GetMapping("/createTicket/{id}")
    public String showFormCreate(Model model,
                                 @PathVariable("id") Long matchId,
                                 @PathVariable("teamId") Long teamId) {

        Ticket ticket = new Ticket();

        List<Stadium> stadiumList = stadiumService.findAll();
        MatchSchedule matchScheduleToCreateTicket = matchScheduleService.findById(matchId);

        String stadiumToMatch = "";
        String address = "";

        for (Stadium stadium : stadiumList) {
            if (Objects.equals(stadium.getTeam().getId(), matchScheduleToCreateTicket.getHomeTeam().getId())) {
                stadiumToMatch = stadium.getName();
                address = stadium.getAddress();
                break;
            }
        }

        ticket.setAwayTeam(matchScheduleToCreateTicket.getAwayTeam().getName());
        ticket.setHomeTeam(matchScheduleToCreateTicket.getHomeTeam().getName());
        ticket.setAddress(address);
        ticket.setStadium(stadiumToMatch);
        ticket.setDateMatch(matchScheduleToCreateTicket.getMatchDate());
        ticket.setTimeMatch(matchScheduleToCreateTicket.getMatchTime());

        model.addAttribute("ticket", ticket);
        model.addAttribute("teamId", teamId);

        return "ticket/create";
    }

    @GetMapping("/optionToCreateTicket")
    public String showOptionToCreateTicket(Model model,
                                           @RequestParam(value = "page", defaultValue = "0") int page,
                                           @RequestParam(value = "size", defaultValue = "100") int size,
                                           @PathVariable("teamId") Long teamId) {

        Pageable pageable = PageRequest.of(page, size);
        List<MatchSchedule> matchScheduleList = matchScheduleService.getAllMatches(pageable).getContent();
        List<Ticket> ticketList = ticketService.findAll(pageable).getContent();

        List<MatchSchedule> matchScheduleListToShow = new ArrayList<>();

        for (MatchSchedule match : matchScheduleList) {
            long between = ChronoUnit.DAYS.between(LocalDate.now(), match.getMatchDate());
            if (between < 1 || between > 20) continue;
            if (!Objects.equals(match.getHomeTeam().getId(), teamId)) continue;

            boolean hasTicket = false;
            for (Ticket t : ticketList) {
                boolean sameHomeTeam = t.getHomeTeam().equals(match.getHomeTeam().getName());
                boolean sameDate = t.getDateMatch().equals(match.getMatchDate());
                boolean sameTime = t.getTimeMatch().equals(match.getMatchTime());

                if (sameHomeTeam && sameDate && sameTime) {
                    hasTicket = true;
                    break;
                }
            }
            if (!hasTicket) {
                matchScheduleListToShow.add(match);
            }
        }
        if (!matchScheduleListToShow.isEmpty()){
            model.addAttribute("matchScheduleListToShow", matchScheduleListToShow);
            model.addAttribute("teamId", teamId);
        } else {
            model.addAttribute("mess", "Không còn trận đấu nào để tạo vé!");
        }


        return "ticket/optionToCreateTicket";
    }

    /**
     * Lưu vé mới tạo
     */
    @PostMapping("/saveCreateTicket")
    public String saveCreateTicket(@ModelAttribute Ticket ticket,
                                   @PathVariable("teamId") Long teamId) {

        boolean check = ticketService.save(ticket);
        // nếu cần bạn có thể thêm RedirectAttributes để báo thành công/thất bại

        return "redirect:/owner/tickets/" + teamId;
    }

    /**
     * (Optional) Form update – hiện tại bạn chưa dùng, nên mình để TODO
     * Nếu muốn dùng thì nên truyền ticketId trong path: /update/{ticketId}
     */
    @GetMapping("/update")
    public String showFormUpdate(Model model,
                                 @RequestParam("id") Integer id,
                                 @PathVariable("teamId") Long teamId) {
        // TODO: load ticket theo id rồi nhét vào model
        // Ticket ticket = ticketService.findById(id);
        // model.addAttribute("ticket", ticket);
        model.addAttribute("teamId", teamId);
        return "ticket/create";
    }

    /**
     * Xóa vé
     */
    @Transactional
    @GetMapping("/delete/{ticketId}")
    public String delete(@PathVariable("ticketId") Integer ticketId,
                         @PathVariable("teamId") Long teamId) {

        Ticket ticketToDelete = ticketService.findById(ticketId);
        if (ticketToDelete != null) {
            ticketService.delete(ticketToDelete);
        }
        return "redirect:/owner/tickets/" + teamId;
    }
}
