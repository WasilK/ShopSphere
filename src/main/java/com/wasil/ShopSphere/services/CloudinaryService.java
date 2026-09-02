package com.wasil.ShopSphere.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.SneakyThrows;
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

    public String uploadImage(MultipartFile image) {

        try {

            Map<?, ?> result = cloudinary.uploader().upload(
                    image.getBytes(),
                    Map.of(
                            "folder", "shopsphere/products"
                    )
            );

            return result.get("secure_url").toString();

        } catch (IOException e) {

            throw new RuntimeException(
                    "Failed to upload image to Cloudinary",
                    e
            );
        }
    }

    public void deleteImage(String imageUrl) {

        String publicId = extractPublicId(imageUrl);

        try {
            cloudinary.uploader().destroy(
                    publicId,
                    ObjectUtils.emptyMap()
            );
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to delete image from Cloudinary"
            );
        }
    }

    private String extractPublicId(String imageUrl) {

        // Example URL:
        // https://res.cloudinary.com/demo/image/upload/v1234567890/shopsphere/products/abc123.jpg

        String uploadPart = "/upload/";

        int uploadIndex = imageUrl.indexOf(uploadPart);

        if (uploadIndex == -1) {
            throw new IllegalArgumentException(
                    "Invalid Cloudinary image URL"
            );
        }

        String publicIdWithExtension =
                imageUrl.substring(
                        uploadIndex + uploadPart.length()
                );

        // Remove version if present
        // v1234567890/shopsphere/products/abc123.jpg
        if (publicIdWithExtension.startsWith("v")) {

            int slashIndex =
                    publicIdWithExtension.indexOf("/");

            if (slashIndex != -1) {
                publicIdWithExtension =
                        publicIdWithExtension.substring(
                                slashIndex + 1
                        );
            }
        }

        // Remove file extension
        int dotIndex =
                publicIdWithExtension.lastIndexOf(".");

        if (dotIndex != -1) {
            publicIdWithExtension =
                    publicIdWithExtension.substring(
                            0,
                            dotIndex
                    );
        }

        return publicIdWithExtension;
    }
}
