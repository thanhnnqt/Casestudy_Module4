package com.example.premier_league.controller;

import com.example.premier_league.vnpayconfig.VNPayConfig;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Controller
@RequestMapping("/vnpay_return")
public class VNPayReturnController {
    @GetMapping
    public String result(HttpServletRequest request, Model model) {
        // 1. Lấy tất cả tham số trả về từ VNPay
        Map<String, String> fields = new HashMap<>();
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements(); ) {
            String fieldName = params.nextElement();
            String fieldValue = request.getParameter(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                fields.put(fieldName, fieldValue);
            }
        }

        // 2. Lấy vnp_SecureHash và vnp_ResponseCode
        String vnp_SecureHash = fields.remove("vnp_SecureHash");
        String responseCode = fields.get("vnp_ResponseCode");

        // 3. Tạo hashData
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames); // Sắp xếp tham số theo thứ tự bảng chữ cái

        StringBuilder hashData = new StringBuilder();
        for (int i = 0; i < fieldNames.size(); i++) {
            String fieldName = fieldNames.get(i);
            String fieldValue = fields.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                if (hashData.length() > 0) {
                    hashData.append('&');
                }
                hashData.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8))
                        .append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
            }
        }

        // Debug
        System.out.println("Return fields     : " + fields);
        System.out.println("Return hashData   : " + hashData);
        System.out.println("Return vnp_SecureHash : " + vnp_SecureHash);
        System.out.println("Return responseCode   : " + responseCode);
        System.out.println("Return SECRET_KEY     : " + VNPayConfig.vnp_HashSecret);

        // 4. Kiểm tra SECRET_KEY
        if (VNPayConfig.vnp_HashSecret == null || VNPayConfig.vnp_HashSecret.isEmpty()) {
            model.addAttribute("mess", "Khóa bí mật chưa được cấu hình");
            return "vnpay/fail";
        }

        // 5. Kiểm tra hashData
        if (hashData.length() == 0) {
            model.addAttribute("mess", "Không có tham số hợp lệ để xác minh chữ ký");
            return "vnpay/fail";
        }

        // 6. Tạo chữ ký server-side
        String signValue = VNPayConfig.hmacSHA512(VNPayConfig.vnp_HashSecret, hashData.toString());
        System.out.println("Return signValue      : " + signValue);

        // 7. Kiểm tra chữ ký + mã phản hồi
        if (signValue.equals(vnp_SecureHash) && "00".equals(responseCode)) {
            model.addAttribute("mess", "Thanh toán thành công!");
            model.addAttribute("responseCode", responseCode);
            model.addAttribute("fields", fields);
            return "vnpay/success"; // -> templates/vnpay/success.html
        } else {
            String errorMessage = "Thanh toán thất bại! Mã lỗi: "
                    + (responseCode != null ? responseCode : "Không xác định");

            if (!signValue.equals(vnp_SecureHash)) {
                errorMessage += " (Chữ ký không hợp lệ: signValue=" + signValue
                        + ", vnp_SecureHash=" + vnp_SecureHash + ")";
            }

            model.addAttribute("message", errorMessage);
            model.addAttribute("responseCode", responseCode);
            model.addAttribute("fields", fields);
            return "vnpay/fail";
        }
    }
}