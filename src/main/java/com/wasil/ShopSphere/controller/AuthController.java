package com.wasil.ShopSphere.controller;

import com.wasil.ShopSphere.dto.auth.LoginRequest;
import com.wasil.ShopSphere.dto.auth.LoginResponse;
import com.wasil.ShopSphere.services.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    public AuthController(AuthService authService){
        this.authService = authService;
    }
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request){
        authService.login(request);
        return new LoginResponse(
                "Login successful"
        );
    }
}
