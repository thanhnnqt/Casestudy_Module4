package com.example.premier_league.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class OwnerDto {

    // SỬA: Đổi tên id -> ownerId để tránh trùng với Account ID
    private Long ownerId;

    @NotBlank(message = "Tên chủ sở hữu không được để trống")
    private String name;

    @NotNull(message = "Ngày sinh không được để trống")
    @Past(message = "Ngày sinh phải là một ngày trong quá khứ")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dob;

    @Pattern(regexp = "^(0|\\+?84)(\\d{9})$", message = "Số điện thoại không hợp lệ (gồm 10 số)")
    private String phoneNumber;

    @NotNull(message = "Vui lòng chọn tài khoản quản lý")
    private Long accountId;

    @NotNull(message = "Vui lòng chọn đội bóng sở hữu")
    private Long teamId;
}