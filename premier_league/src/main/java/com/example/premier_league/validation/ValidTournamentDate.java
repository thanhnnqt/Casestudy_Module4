package com.example.premier_league.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = TournamentDateValidator.class)
@Target({ElementType.TYPE}) // Áp dụng cho cả Class (để so sánh 2 trường)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidTournamentDate {
    String message() default "Ngày kết thúc phải sau ngày bắt đầu";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}