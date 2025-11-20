package com.example.premier_league.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AccountController {

    // Trang login
    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {

        // Spring Security tự động redirect kèm ?error khi sai mật khẩu
        if (error != null) {
            model.addAttribute("toastTitle", "Đăng nhập thất bại");
            model.addAttribute("toastMessage", "Tên đăng nhập hoặc mật khẩu không đúng!");
            model.addAttribute("toastType", "error"); // Để đổi màu nếu thích
        }

        if (logout != null) {
            model.addAttribute("toastTitle", "Đăng xuất");
            model.addAttribute("toastMessage", "Hẹn gặp lại bạn!");
            model.addAttribute("toastType", "success");
        }

        return "home/login";
    }

    // Trang logout (Spring Security sẽ xử lý logout, đây chỉ để redirect)
    @GetMapping("/logout-success")
    public String logoutPage(Model model) {
        model.addAttribute("msg", "Bạn đã đăng xuất thành công.");
        return "home/login"; // redirect về login
    }
}
