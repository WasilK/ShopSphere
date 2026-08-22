package com.wasil.ShopSphere.services;

import com.wasil.ShopSphere.dto.auth.LoginRequest;
import com.wasil.ShopSphere.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public String login(LoginRequest request) {

        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(
                        request.getUserEmail(),
                        request.getPassword()
                );

        authenticationManager.authenticate(token);
        return jwtService.generateToken(request.getUserEmail());
    }
}
