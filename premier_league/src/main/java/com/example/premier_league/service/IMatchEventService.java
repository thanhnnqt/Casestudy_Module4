package com.example.premier_league.service;

import com.example.premier_league.dto.MatchEventDto;
import com.example.premier_league.dto.MatchEventResponse;
import com.example.premier_league.entity.MatchEvent;
import com.example.premier_league.entity.Match;

import java.util.List;

public interface IMatchEventService {
    MatchEvent createEvent(MatchEvent event);
    List<MatchEvent> listEvents(Long matchId);
    Match findMatchById(Long matchId);
    MatchEvent getEvent(Long id);
    List<MatchEventResponse> getEventsByMatch(Long matchId);

    void addEvent(Long matchId, MatchEventDto dto);
}
