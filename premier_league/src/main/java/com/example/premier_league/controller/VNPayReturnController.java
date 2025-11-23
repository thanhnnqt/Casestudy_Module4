package com.example.premier_league.controller;

import com.example.premier_league.entity.Account;
import com.example.premier_league.service.IAccountService;
import com.example.premier_league.service.impl.IClientTicketService;
import com.example.premier_league.vnpayconfig.VNPayConfig;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPTable;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.lowagie.text.pdf.PdfWriter;


import com.lowagie.text.Rectangle;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.ByteArrayOutputStream;
import java.util.List;

import com.example.premier_league.dto.TicketDto;

import com.lowagie.text.pdf.*;

import java.awt.Color;
import java.util.Locale;

@Controller
@RequestMapping("/vnpay_return")
public class VNPayReturnController {
    private final IAccountService accountService;
    private final IClientTicketService clientTicketService;

    public VNPayReturnController(IAccountService accountService,
                                 IClientTicketService clientTicketService) {
        this.accountService = accountService;
        this.clientTicketService = clientTicketService;
    }

    private void addRow(PdfPTable table, String label, String value,
                        Font labelFont, Font valueFont) {

        PdfPCell c1 = new PdfPCell(new Phrase(label, labelFont)); // 🔥 label dùng font BOLD
        c1.setBorder(Rectangle.NO_BORDER);
        c1.setPadding(4f);

        PdfPCell c2 = new PdfPCell(new Phrase(value, valueFont)); // value font thường
        c2.setBorder(Rectangle.NO_BORDER);
        c2.setPadding(4f);

        table.addCell(c1);
        table.addCell(c2);
    }


//    @GetMapping
//    public String result(HttpServletRequest request, Model model) throws Exception {
//
//        // 1. Lấy tất cả tham số trả về từ VNPay
//        Map<String, String> fields = new HashMap<>();
//        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements(); ) {
//            String fieldName = params.nextElement();
//            String fieldValue = request.getParameter(fieldName);
//            if (fieldValue != null && !fieldValue.isEmpty()) {
//                fields.put(fieldName, fieldValue);
//            }
//        }
//
//        // 2. Lấy vnp_SecureHash và vnp_ResponseCode
//        String vnp_SecureHash = fields.remove("vnp_SecureHash");
//        String responseCode = fields.get("vnp_ResponseCode");
//
//        // 3. Tạo hashData (sắp xếp & encode giống servlet cũ)
//        List<String> fieldNames = new ArrayList<>(fields.keySet());
//        Collections.sort(fieldNames); // Sắp xếp tham số theo thứ tự bảng chữ cái
//
//        StringBuilder hashData = new StringBuilder();
//        for (int i = 0; i < fieldNames.size(); i++) {
//            String fieldName = fieldNames.get(i);
//            String fieldValue = fields.get(fieldName);
//            if (fieldValue != null && !fieldValue.isEmpty()) {
//                if (hashData.length() > 0) {
//                    hashData.append('&');
//                }
//                hashData.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8))
//                        .append('=')
//                        .append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
//            }
//        }
//
//        // Debug
//        System.out.println("Return fields     : " + fields);
//        System.out.println("Return hashData   : " + hashData);
//        System.out.println("Return vnp_SecureHash : " + vnp_SecureHash);
//        System.out.println("Return responseCode   : " + responseCode);
//        System.out.println("Return SECRET_KEY     : " + VNPayConfig.vnp_HashSecret);
//
//        // 4. Kiểm tra SECRET_KEY
//        if (VNPayConfig.vnp_HashSecret == null || VNPayConfig.vnp_HashSecret.isEmpty()) {
//            model.addAttribute("mess", "Khóa bí mật chưa được cấu hình");
//            return "vnpay/fail";
//        }
//
//        // 5. Kiểm tra hashData
//        if (hashData.length() == 0) {
//            model.addAttribute("mess", "Không có tham số hợp lệ để xác minh chữ ký");
//            return "vnpay/fail";
//        }
//
//        // 6. Tạo chữ ký server-side
//        String signValue = VNPayConfig.hmacSHA512(VNPayConfig.vnp_HashSecret, hashData.toString());
//        System.out.println("Return signValue      : " + signValue);
//
//        // 7. Kiểm tra chữ ký + mã phản hồi
//        if (signValue.equals(vnp_SecureHash) && "00".equals(responseCode)) {
//            model.addAttribute("mess", "Thanh toán thành công!");
//            model.addAttribute("responseCode", responseCode);
//            model.addAttribute("fields", fields);
//            return "vnpay/success"; // -> templates/vnpay/success.html
//        } else {
//            String errorMessage = "Thanh toán thất bại! Mã lỗi: "
//                    + (responseCode != null ? responseCode : "Không xác định");
//
//            if (!signValue.equals(vnp_SecureHash)) {
//                errorMessage += " (Chữ ký không hợp lệ: signValue=" + signValue
//                        + ", vnp_SecureHash=" + vnp_SecureHash + ")";
//            }
//
//            model.addAttribute("message", errorMessage);
//            model.addAttribute("responseCode", responseCode);
//            model.addAttribute("fields", fields);
//            return "vnpay/fail";
//        }
//    }

    @GetMapping
    public String result(HttpServletRequest request,
                         HttpSession sessionHttp,
                         Model model) throws Exception {

        Map<String, String> fields = new HashMap<>();

        // Chỉ lấy param VNPay trả về
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements(); ) {
            String fieldName = params.nextElement();

            if (!fieldName.startsWith("vnp_"))
                continue; // cực quan trọng!!!

            String fieldValue = request.getParameter(fieldName);
            if (fieldValue != null && fieldValue.length() > 0) {
                fields.put(fieldName, fieldValue);
            }
        }

        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        String vnp_ResponseCode = request.getParameter("vnp_ResponseCode");
        String vnp_TxnRef = request.getParameter("vnp_TxnRef");

        fields.remove("vnp_SecureHashType");
        fields.remove("vnp_SecureHash");

// Tạo hashData y như servlet gốc
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        for (int i = 0; i < fieldNames.size(); i++) {
            String fieldName = fieldNames.get(i);
            String fieldValue = fields.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                if (i > 0) hashData.append('&');
                hashData.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8))
                        .append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
            }
        }

        String signValue = VNPayConfig.hmacSHA512(VNPayConfig.vnp_HashSecret, hashData.toString());


        TicketDto latestTicket = (TicketDto) sessionHttp.getAttribute("latestTicket");
        model.addAttribute("ticketDto", latestTicket);

        if (signValue.equals(vnp_SecureHash) && "00".equals(vnp_ResponseCode)) {

            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            Account loggedIn = accountService.findByUsername(username).orElse(null);

            if (loggedIn != null && latestTicket != null) {
                clientTicketService.saveFromTicketDto(latestTicket, loggedIn, vnp_TxnRef);
            }

            model.addAttribute("mess", "Thanh toán thành công! Vé của bạn đã được lưu vào lịch sử.");
            return "vnpay/success";
        } else {
            model.addAttribute("mess", "Thanh toán thất bại hoặc không hợp lệ. Vui lòng thử lại.");
            return "vnpay/fail";
        }


    }



    @GetMapping(value = "/printTicket", produces = "application/pdf")
    public ResponseEntity<byte[]> printTicket(HttpSession session) throws Exception {
        TicketDto ticketDto = (TicketDto) session.getAttribute("latestTicket");

        if (ticketDto == null) {
            return ResponseEntity
                    .badRequest()
                    .body("Không tìm thấy thông tin vé trong phiên làm việc.".getBytes("UTF-8"));
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Document document = new Document(PageSize.A5.rotate(), 40, 40, 30, 30);
        PdfWriter.getInstance(document, baos);
        document.open();

        // ====== FONT ======
        Font titleFont = new Font(Font.HELVETICA, 22, Font.BOLD, new Color(25, 118, 210));
        Font subTitleFont = new Font(Font.HELVETICA, 12, Font.NORMAL, new Color(56, 142, 60));
        Font labelFont = new Font(Font.HELVETICA, 11, Font.NORMAL, new Color(66, 66, 66));
        Font valueFont = new Font(Font.HELVETICA, 11, Font.NORMAL, Color.BLACK);
        Font footerFont = new Font(Font.HELVETICA, 9, Font.ITALIC, new Color(117, 117, 117));

        // ====== TITLE ======
        Paragraph title = new Paragraph("HÓA ĐƠN ĐẶT VÉ XEM BÓNG ĐÁ", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(4);
        document.add(title);

        Paragraph subTitle = new Paragraph("Xác nhận đặt vé thành công", subTitleFont);
        subTitle.setAlignment(Element.ALIGN_CENTER);
        subTitle.setSpacingAfter(12);
        document.add(subTitle);

        document.add(new Paragraph(" ")); // spacing

        // ====== FORMAT NGÀY (dd/MM/yyyy) ======
        String formattedDate = ticketDto.getDateMatch() != null
                ? ticketDto.getDateMatch().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                : "";

        // ====== BẢNG THÔNG TIN ======
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setWidths(new float[]{30f, 70f});
        infoTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        String totalFormatted = nf.format(ticketDto.getTotalPay()) + " đ";

        addRow(infoTable, "Trận đấu", ticketDto.getHomeTeam() + " vs " + ticketDto.getAwayTeam(), labelFont, valueFont);
        addRow(infoTable, "Sân vận động", ticketDto.getStadium(), labelFont, valueFont);
        addRow(infoTable, "Địa chỉ", ticketDto.getAddress(), labelFont, valueFont);
        addRow(infoTable, "Ngày", formattedDate, labelFont, valueFont); // <--- NGÀY CHUẨN VIỆT NAM
        addRow(infoTable, "Giờ", String.valueOf(ticketDto.getTimeMatch()), labelFont, valueFont);
        addRow(infoTable, "Khu", ticketDto.getStandSession(), labelFont, valueFont);
        addRow(infoTable, "Số ghế", ticketDto.getSeatNumber(), labelFont, valueFont);
        addRow(infoTable, "Số lượng ghế", String.valueOf(ticketDto.getQuantity()), labelFont, valueFont);
        addRow(infoTable, "Tổng tiền", totalFormatted, labelFont, valueFont);

        PdfPTable cardTable = new PdfPTable(1);
        cardTable.setWidthPercentage(100);

        PdfPCell cardCell = new PdfPCell();
        cardCell.setPadding(12f);
        cardCell.setBorderWidth(1.2f);
        cardCell.setBorderColor(new Color(200, 200, 200));
        cardCell.setBackgroundColor(new Color(250, 250, 250));
        cardCell.addElement(infoTable);

        cardTable.addCell(cardCell);
        cardTable.setSpacingAfter(15);
        document.add(cardTable);

        // FOOTER
        Paragraph note = new Paragraph(
                "Vui lòng có mặt tại sân vận động trước giờ bóng lăn ít nhất 30 phút để ổn định chỗ ngồi.",
                footerFont
        );
        note.setAlignment(Element.ALIGN_CENTER);
        note.setSpacingBefore(8);
        document.add(note);

        Paragraph thanks = new Paragraph("Cảm ơn bạn đã đặt vé!", footerFont);
        thanks.setAlignment(Element.ALIGN_CENTER);
        document.add(thanks);

        document.close();

        byte[] pdfBytes = baos.toByteArray();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "ticket.pdf");
        headers.setContentLength(pdfBytes.length);

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

}