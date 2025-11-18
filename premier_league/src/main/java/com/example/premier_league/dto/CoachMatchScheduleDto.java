package com.example.premier_league.dto;

import com.example.premier_league.entity.MatchSchedule;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CoachMatchScheduleDto {

    private MatchSchedule match;
    private boolean lineupRegistered; // Đã đăng ký hay chưa?

    public CoachMatchScheduleDto(MatchSchedule match, boolean lineupRegistered) {
        this.match = match;
        this.lineupRegistered = lineupRegistered;
    }
}