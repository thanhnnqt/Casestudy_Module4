package com.example.premier_league.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TeamDto {

    private Integer id;

    @NotBlank(message = "Tên đội bóng không được để trống")
    @Size(max = 100, message = "Tên đội bóng không được vượt quá 100 ký tự")
    private String name;

    @NotBlank(message = "Tên viết tắt không được để trống")
    @Size(max = 10, message = "Tên viết tắt không được vượt quá 10 ký tự")
    private String shortName;

    @NotBlank(message = "Quốc gia không được để trống")
    private String country;

    @NotBlank(message = "Thành phố không được để trống")
    private String city;

    @NotBlank(message = "Sân vận động không được để trống")
    private String stadium;

    @NotBlank(message = "Tên huấn luyện viên không được để trống")
    private String coachName;

    // Logo URL có thể để trống
    @Pattern(
            regexp = "^(http|https)://.*$",
            message = "Logo phải là một URL hợp lệ"
    )
    @Size(max = 255, message = "URL quá dài")
    private String logoUrl;

    @Size(max = 500, message = "Mô tả không được vượt quá 500 ký tự")
    private String description;

    @Min(value = 0, message = "Tổng số cầu thủ không hợp lệ")
    private int totalPlayers;

    @Min(value = 0, message = "Số trận thắng không hợp lệ")
    private int winCount;

    @Min(value = 0, message = "Số trận hòa không hợp lệ")
    private int drawCount;

    @Min(value = 0, message = "Số trận thua không hợp lệ")
    private int loseCount;

    @Min(value = 0, message = "Số điểm không hợp lệ")
    private int points;
}
