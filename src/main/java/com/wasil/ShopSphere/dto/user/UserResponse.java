package com.wasil.ShopSphere.dto.user;

import com.wasil.ShopSphere.model.Role;
import lombok.Data;

import java.time.Instant;

@Data
public class UserResponse {
    private Long userId;
    private String firstName;
    private String lastName;
    private String userEmail;
    private String userPhone;
    private Boolean isActive;
    private Role role;
    private Instant userCreatedAt;
    private Instant userUpdatedAt;
}
