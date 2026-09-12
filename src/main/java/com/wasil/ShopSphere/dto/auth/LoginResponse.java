package com.wasil.ShopSphere.dto.auth;


import com.wasil.ShopSphere.model.Role;
import lombok.Data;

@Data
public class LoginResponse {
    private String message;
    private String token;
    private Role role;

    public LoginResponse(String message, String token, Role role) {
        this.message = message;
        this.token = token;
        this.role = role;
    }
}
