package com.example.premier_league.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class PrizeDto {

    private Long prizeId;

    @NotBlank(message = "Tên giải thưởng không được để trống")
    private String name;

    @NotBlank(message = "Loại giải thưởng không được để trống")
    private String type;

    @NotNull(message = "Tiền thưởng không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Tiền thưởng phải là số dương")
    private Double amount;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate awardedDate;

    private Long teamId;
    private Long playerId;
}