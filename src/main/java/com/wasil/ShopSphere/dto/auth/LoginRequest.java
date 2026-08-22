package com.wasil.ShopSphere.dto.auth;

import lombok.Data;

@Data
public class LoginRequest {
    private String userEmail;
    private String password;
}
