package com.example.premier_league.validation;

import com.example.premier_league.dto.TournamentDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TournamentDateValidator implements ConstraintValidator<ValidTournamentDate, TournamentDto> {

    @Override
    public boolean isValid(TournamentDto dto, ConstraintValidatorContext context) {
        if (dto.getStartDate() == null || dto.getEndDate() == null) {
            return true; // Để @NotNull ở các trường riêng lẻ xử lý việc null
        }

        // Logic: Ngày kết thúc phải sau ngày bắt đầu
        boolean isValid = dto.getEndDate().isAfter(dto.getStartDate());

        if (!isValid) {
            // Gán lỗi này vào trường 'endDate' để hiển thị đúng chỗ trên form HTML
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("endDate")
                    .addConstraintViolation();
        }

        return isValid;
    }
}