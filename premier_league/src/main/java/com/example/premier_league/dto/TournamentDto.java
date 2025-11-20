package com.example.premier_league.dto;

import com.example.premier_league.validation.ValidTournamentDate;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@ValidTournamentDate
public class TournamentDto {

    private Long id;

    // Tên giải: Cho phép nhập, giới hạn độ dài để tránh lỗi DB
    @Size(max = 100, message = "Tên giải đấu không được vượt quá 100 ký tự")
    private String name;

    @NotBlank(message = "Vui lòng nhập tên mùa giải")
    // Regex giải thích:
    // ^20 : Bắt đầu bằng số 20 (tức là năm 20xx, tránh nhập năm 19xx hay 30xx vô lý)
    // \d{2}: Theo sau là 2 chữ số bất kỳ
    // / : Dấu gạch chéo
    // 20\d{2}: Năm kết thúc cũng dạng 20xx
    @Pattern(regexp = "^20\\d{2}/20\\d{2}$", message = "Định dạng mùa giải không hợp lệ (VD: 2025/2026)")
    private String season;

    @NotNull(message = "Vui lòng chọn ngày bắt đầu")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @NotNull(message = "Vui lòng chọn ngày kết thúc")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    // @Future(message = "Ngày kết thúc phải ở tương lai") // Có thể thêm nếu muốn chặn sửa giải cũ
    private LocalDate endDate;

    @Size(max = 500, message = "Mô tả không được vượt quá 500 ký tự")
    private String description;
}