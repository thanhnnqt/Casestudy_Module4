package com.example.premier_league.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {

        Object message = request.getAttribute("javax.servlet.error.message");

        model.addAttribute("message",
                message != null ? message : "Đã xảy ra lỗi không xác định");

        return "error"; // => templates/error.html
    }
}
