package com.example.premier_league.controller;

import com.example.premier_league.entity.MatchSchedule;
import com.example.premier_league.entity.Stadium;
import com.example.premier_league.entity.Ticket;
import com.example.premier_league.service.IMatchScheduleService;
import com.example.premier_league.service.impl.IStadiumService;
import com.example.premier_league.service.impl.ITicketService;
import com.example.premier_league.service.impl.ITicketTypeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping("/admin/tickets/{teamId}")
public class TicketController {
    final ITicketService ticketService;
    final ITicketTypeService ticketTypeService;
    final IMatchScheduleService matchScheduleService;
    final IStadiumService stadiumService;

    public TicketController(ITicketService ticketService, ITicketTypeService ticketTypeService, IMatchScheduleService matchScheduleService, IStadiumService stadiumService) {
        this.ticketService = ticketService;
        this.ticketTypeService = ticketTypeService;
        this.matchScheduleService = matchScheduleService;
        this.stadiumService = stadiumService;
    }

    @GetMapping()
    public String showTicketList(Model model, @RequestParam(value = "page", defaultValue = "0") int page, @RequestParam(value = "size", defaultValue = "2") int size, @PathVariable(value = "teamId") Integer teamId) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Ticket> ticketPage = ticketService.findAll(pageable);
        if (ticketPage.getContent().isEmpty()) {
            model.addAttribute("mess", "The list is empty");
        } else {
            model.addAttribute("ticketPage", ticketPage);
        }
        System.out.println(ticketPage.getContent().size());
        return "ticket/list";
    }

    @GetMapping("/create")
    public String showFormCreate(Model model, @RequestParam(value = "id") Long id, @PathVariable(value = "teamId") String teamId) {
        Ticket ticket = new Ticket();
        List<Stadium> stadiumList = stadiumService.findAll();
        MatchSchedule matchScheduleToCreateTicket = matchScheduleService.findById(id);
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
        return "ticket/create";
    }

    @GetMapping("/optionToCreateTicket")
    public String showOptionToCreateTicket(Model model, @RequestParam(value = "page", defaultValue = "0", required = false) int page,
                                           @RequestParam(value = "size", defaultValue = "100", required = false) int size, @PathVariable(value = "teamId") Integer teamId) {
        Pageable pageable = PageRequest.of(page, size);
        Page<MatchSchedule> matchScheduleList = matchScheduleService.getAllMatches(pageable);
        List<MatchSchedule> matchScheduleListToShow = new ArrayList<>();
        MatchSchedule matchScheduleTest = matchScheduleList.getContent().get(0);
        Long idHomeTeam = matchScheduleTest.getHomeTeam().getId();
        long between = 0;
        for (MatchSchedule matchSchedule : matchScheduleList) {
            between = ChronoUnit.DAYS.between(LocalDate.now(), matchSchedule.getMatchDate());
            if (between >= 1 && between <= 20 && Objects.equals(matchSchedule.getHomeTeam().getId(), idHomeTeam)) {
                matchScheduleListToShow.add(matchSchedule);
            }
        }
        model.addAttribute("matchScheduleListToShow", matchScheduleListToShow);
        System.out.println(matchScheduleListToShow.size());
        return "ticket/optionToCreateTicket";
    }

    @PostMapping("/saveCreateTicket")
    public String saveCreateTicket(@ModelAttribute Ticket ticket, @PathVariable(value = "teamId") Integer teamId) {
        boolean check = ticketService.create(ticket);
        if (check) {
            System.out.println("Created!");
        } else {
            System.out.println("Fail!");
        }
        return "redirect:/admin/tickets";
    }

    @GetMapping("/update")
    public String showFormUpdate(Model model, @RequestParam("id") int id, @PathVariable(value = "teamId") Integer teamId) {
        model.addAttribute("id", id);
        System.out.println(id);
        return "ticket/create";
    }
}
