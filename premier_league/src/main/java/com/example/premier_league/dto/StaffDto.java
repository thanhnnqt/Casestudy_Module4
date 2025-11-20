package com.example.premier_league.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class StaffDto {

    private Integer id;

    @NotBlank(message = "Tên không được để trống")
    private String fullName;

    // 2. Thêm dòng này cho Ngày sinh
    @NotNull(message = "Ngày sinh không được để trống")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Vui lòng chọn giới tính")
    private String gender;

    @NotBlank(message = "Quốc tịch không được để trống")
    private String nationality;

    @NotBlank(message = "Vị trí công tác không được để trống")
    private String position;

    @NotBlank(message = "Vai trò không được để trống")
    private String role;

    @NotNull(message = "Ngày gia nhập không được để trống")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate joinDate;

    @Pattern(
            regexp = "^(0|\\+?84)(\\d{9})$",
            message = "Số điện thoại không hợp lệ"
    )
    private String phoneNumber;

    @Email(message = "Email không hợp lệ")
    @NotBlank(message = "Email không được để trống")
    private String email;

    @NotBlank(message = "Trạng thái không được để trống")
    private String status;

    // Avatar chỉ là URL nên có thể để trống
    private String avatarUrl;

    // Không truyền Team mà truyền teamId
//    @NotNull(message = "Vui lòng chọn đội bóng")
    private Long teamId;

}
