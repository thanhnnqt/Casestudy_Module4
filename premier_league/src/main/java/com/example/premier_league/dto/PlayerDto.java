package com.example.premier_league.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PlayerDto {
    private Long id;

    @NotBlank(message = "Tên cầu thủ không được để trống")
    private String name;

    @NotNull(message = "Ngày sinh không được để trống")
    private LocalDate dob;

    @NotBlank(message = "Kinh nghiệm không được để trống")
    private String experience;

    @NotBlank(message = "Vị trí không được để trống")
    private String position;

    private String avatar;

    @NotNull(message = "Team không được để trống")
    private Long teamId;
}
