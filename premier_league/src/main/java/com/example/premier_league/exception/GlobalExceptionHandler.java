package com.example.premier_league.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public String handleMismatch(MethodArgumentTypeMismatchException ex, Model model) {
        model.addAttribute("message", " Vui lòng nhập số.");
        return "error";
    }

    @ExceptionHandler(StaffNotFoundException.class)
    public String handleNotFound(StaffNotFoundException ex, Model model) {
        model.addAttribute("message", " " + ex.getMessage());
        return "error";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneral(Exception ex, Model model) {
        model.addAttribute("message", " Có lỗi xảy ra: " + ex.getMessage());
        return "error";
    }
}
