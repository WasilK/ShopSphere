package com.wasil.ShopSphere.controller;

import com.wasil.ShopSphere.dto.auth.LoginRequest;
import com.wasil.ShopSphere.dto.auth.LoginResponse;
import com.wasil.ShopSphere.dto.user.UserRequest;
import com.wasil.ShopSphere.dto.user.UserResponse;
import com.wasil.ShopSphere.security.JwtService;
import com.wasil.ShopSphere.security.TokenBlacklistService;
import com.wasil.ShopSphere.services.AuthService;
import com.wasil.ShopSphere.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication API's")
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final UserService userService;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthController(
            AuthService authService,
            UserService userService,
            JwtService jwtService,
            TokenBlacklistService tokenBlacklistService) {
        this.authService = authService;
        this.userService = userService;
        this.jwtService = jwtService;
        this.tokenBlacklistService = tokenBlacklistService;
    }
    @Operation(security = {}, summary = "To login.")
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request){
        return authService.login(request);
    }
    @Operation(security = {}, summary = "To signup a new account.")
    @PostMapping("/register")
    public UserResponse register(@Valid @RequestBody UserRequest request){
        return userService.addUser(request);
    }

    /*
     * Revokes the presented token by adding its jti to the Redis denylist
     * (TTL = the token's own remaining validity, so the entry cleans
     * itself up). JwtAuthenticationFilter checks this denylist on every
     * subsequent request, so the token stops working immediately instead
     * of remaining valid until it naturally expires.
     */
    @Operation(summary = "To logout.")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {

            String token = authHeader.substring(7);
            String jti = jwtService.extractJti(token);
            long remainingMs = jwtService.getRemainingValidityMillis(token);

            tokenBlacklistService.blacklist(jti, remainingMs);
        }

        return ResponseEntity.noContent().build();
    }
}
