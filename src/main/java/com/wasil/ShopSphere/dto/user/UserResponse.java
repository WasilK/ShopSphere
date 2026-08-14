package com.wasil.ShopSphere.dto.user;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Data
public class UserResponse {
    private Long userId;
    private String userName;
    private String userEmail;
    private String userPhone;
    private Instant userCreatedAt;
    private Instant userUpdatedAt;
}
