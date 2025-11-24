package com.example.premier_league.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String uploadFile(MultipartFile file) {
        try {
            // Upload file lên Cloudinary
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            // Trả về đường dẫn URL của ảnh
            return (String) uploadResult.get("url");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}