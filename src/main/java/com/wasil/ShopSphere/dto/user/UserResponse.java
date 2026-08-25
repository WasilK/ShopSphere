package com.wasil.ShopSphere.dto.user;

import lombok.Data;

import java.time.Instant;

@Data
public class UserResponse {
    private Long userId;
    private String firstName;
    private String lastName;
    private String userEmail;
    private String userPhone;
    private Instant userCreatedAt;
    private Instant userUpdatedAt;
}
