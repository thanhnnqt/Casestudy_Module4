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
    private final IStadiumRowService stadiumRowService;
    private final ISeatService seatService;

    public TicketClientController(MatchScheduleService matchScheduleService, ITicketService ticketService, ITicketTypeService ticketTypeService, ISessionService sessionService, IStadiumService stadiumService, IStadiumRowService stadiumRowService, ISeatService seatService) {
        this.matchScheduleService = matchScheduleService;
        this.ticketService = ticketService;
        this.ticketTypeService = ticketTypeService;
        this.sessionService = sessionService;
        this.stadiumService = stadiumService;
        this.stadiumRowService = stadiumRowService;
        this.seatService = seatService;
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

        for (Session session : sessionList) {
            List<StadiumRow> rowList = stadiumRowService.findAllBySessionId(session.getId());

            for (StadiumRow row : rowList) {
                List<Seat> seatList = seatService.findAllByRowId(row.getId());
                row.setSeatList(seatList);
            }

            session.setRowList(rowList);
        }

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
    public String showInfoTicket(@ModelAttribute TicketDto ticketDto, Model model) {

        List<String> seatNumberList = new ArrayList<>();

        // Nếu seatNumber có dạng "A1,A2,B10" → set quantity = số ghế
        if (ticketDto.getSeatNumber() != null && !ticketDto.getSeatNumber().trim().isEmpty()) {
            String[] seats = ticketDto.getSeatNumber().split("\\s*,\\s*");
            seatNumberList = List.of(seats); // Java 9+
            ticketDto.setQuantity(seats.length);

            // ⭐ Đánh dấu các ghế này là đã đặt trong DB
            seatService.markSeatsOccupiedBySeatNumbers(seatNumberList);
        }

        TicketType ticketType = ticketTypeService.findById(ticketDto.getTicketType().getId());
        int totalPay = ticketType.getPrice() * ticketDto.getQuantity();

        model.addAttribute("ticketDto", ticketDto);
        model.addAttribute("totalPay", totalPay);
        model.addAttribute("mess", "Đã tạo vé thành công");

        return "ticket/infoTicketOfClient";
    }
}
