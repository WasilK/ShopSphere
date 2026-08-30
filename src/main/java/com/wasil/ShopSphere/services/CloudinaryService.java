package com.wasil.ShopSphere.services;

import com.cloudinary.Cloudinary;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.util.Map;

@Service
public class CloudinaryService {
    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @SneakyThrows
    public String uploadImage(MultipartFile image) {

        Map<?, ?> result = cloudinary.uploader().upload(
                image.getBytes(),
                Map.of(
                        "folder", "shopsphere/products"
                )
        );

        return result.get("secure_url").toString();
    }
}
