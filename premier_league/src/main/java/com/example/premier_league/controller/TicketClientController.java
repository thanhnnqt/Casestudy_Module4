package com.example.premier_league.controller;

import com.example.premier_league.dto.TicketDto;
import com.example.premier_league.entity.*;
import com.example.premier_league.service.impl.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/tickets")
public class TicketClientController {
    private final MatchScheduleService matchScheduleService;
    private final ITicketService ticketService;
    private final ITicketTypeService ticketTypeService;
    private final ISessionService sessionService;
    private final IStadiumService stadiumService;

    public TicketClientController(MatchScheduleService matchScheduleService, ITicketService ticketService, ITicketTypeService ticketTypeService, ISessionService sessionService, IStadiumService stadiumService, ISeatService seatService) {
        this.matchScheduleService = matchScheduleService;
        this.ticketService = ticketService;
        this.ticketTypeService = ticketTypeService;
        this.sessionService = sessionService;
        this.stadiumService = stadiumService;
    }

    @GetMapping("/create")
    public String tickets(Model model, @RequestParam(name = "id") Long id) {
        MatchSchedule matchSchedule = matchScheduleService.findById(id);
        List<TicketType> ticketTypeList = ticketTypeService.findAll();

        Ticket ticket = ticketService.findByHomeTeamAndAwayTeam(
                matchSchedule.getHomeTeam().getName(),
                matchSchedule.getAwayTeam().getName()
        );

        if (ticket == null) {
            model.addAttribute("mess", "Chưa mở bán vé, vui lòng chọn trận đấu khác!");
            return "ticket/clientCreate";
        }

        Stadium stadium = stadiumService.findByName(ticket.getStadium());
        List<Session> sessionList = sessionService.findAllByStadium_Id(stadium.getId());

        TicketDto ticketDto = new TicketDto();
        ticketDto.setHomeTeam(ticket.getHomeTeam());
        ticketDto.setAwayTeam(ticket.getAwayTeam());
        ticketDto.setStadium(ticket.getStadium());
        ticketDto.setAddress(ticket.getAddress());
        ticketDto.setDateMatch(ticket.getDateMatch());
        ticketDto.setTimeMatch(ticket.getTimeMatch());

        model.addAttribute("ticketTypeList", ticketTypeList);
        model.addAttribute("ticketDto", ticketDto);
        model.addAttribute("sessionList", sessionList);

        return "ticket/clientCreate";
    }

    @PostMapping("/saveCreate")
    public String saveCreate(@ModelAttribute("ticketDto") TicketDto ticketDto, Model model) {

        if (ticketDto.getStandSession() == null || ticketDto.getStandSession().isBlank()) {
            model.addAttribute("mess", "Vui lòng chọn khu ghế.");
            model.addAttribute("ticketDto", ticketDto);
            return "ticket/infoTicketOfClient";
        }

        if (ticketDto.getQuantity() == null || ticketDto.getQuantity() <= 0) {
            model.addAttribute("mess", "Vui lòng nhập số lượng ghế hợp lệ.");
            model.addAttribute("ticketDto", ticketDto);
            return "ticket/infoTicketOfClient";
        }

        Session session = sessionService.findByNameAndStadiumName(ticketDto.getStandSession(), ticketDto.getStadium());

        if (session == null) {
            model.addAttribute("mess", "Không tìm thấy khu ghế tương ứng.");
            model.addAttribute("ticketDto", ticketDto);
            return "ticket/infoTicketOfClient";
        }

        int capacity = session.getCapacity() == null ? 0 : session.getCapacity();
        int lastAssigned = session.getLastAssignedSeat() == null ? 0 : session.getLastAssignedSeat();
        int requestQuantity = ticketDto.getQuantity();


        if (lastAssigned + requestQuantity > capacity) {
            model.addAttribute("mess", "Số lượng ghế còn lại trong khu "
                    + session.getName() + " không đủ. Vui lòng chọn lại.");
            model.addAttribute("ticketDto", ticketDto);
            return "ticket/infoTicketOfClient";
        }


        String standName = session.getName();
        String shortStand = standName.substring(standName.lastIndexOf(" ") + 1);

        List<String> newSeats = new ArrayList<>();
        for (int i = 1; i <= requestQuantity; i++) {
            int seatIndex = lastAssigned + i;
            newSeats.add(shortStand + seatIndex);
        }

        session.setLastAssignedSeat(lastAssigned + requestQuantity);
        sessionService.save(session);

        String seatNumberString = String.join(",", newSeats);
        ticketDto.setSeatNumber(seatNumberString);

        TicketType ticketType = ticketTypeService.findById(ticketDto.getTicketType().getId());
        Integer totalPay = requestQuantity * ticketType.getPrice();
        ticketDto.setTotalPay(totalPay);

        model.addAttribute("ticketDto", ticketDto);
        model.addAttribute("mess", "Đặt vé thành công!");

        return "ticket/infoTicketOfClient";
    }

}
