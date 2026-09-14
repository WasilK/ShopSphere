package com.wasil.ShopSphere.services;

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

    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024 * 1024; // 5MB
    private static final java.util.Set<String> ALLOWED_CONTENT_TYPES = java.util.Set.of(
            "image/jpeg", "image/png", "image/webp"
    );

    public String uploadImage(MultipartFile image) {

        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("Image file must not be empty");
        }

        if (image.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException(
                    "Image file must not exceed 5MB"
            );
        }

        String contentType = image.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Unsupported image type. Allowed types: JPEG, PNG, WEBP"
            );
        }

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
