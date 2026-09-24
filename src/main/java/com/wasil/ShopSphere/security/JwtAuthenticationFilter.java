package com.wasil.ShopSphere.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            CustomUserDetailsService userDetailsService,
            TokenBlacklistService tokenBlacklistService) {

        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {
        System.out.println(
                "JWT FILTER HIT: "
                        + request.getMethod()
                        + " "
                        + request.getRequestURI()
                        + " | Authorization present: "
                        + (request.getHeader("Authorization") != null)
        );
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            String jwt = authHeader.substring(7);
            String email = jwtService.extractUsername(jwt);
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            String jti = jwtService.extractJti(jwt);

            if (jwtService.isTokenValid(jwt, userDetails)
                    && userDetails.isEnabled()
                    && !tokenBlacklistService.isBlacklisted(jti)
                    && SecurityContextHolder.getContext().getAuthentication() == null) {
                Authentication authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                SecurityContextHolder.getContext()
                        .setAuthentication(authentication);
            }
        }catch(Exception e){
            SecurityContextHolder.clearContext();
            e.printStackTrace();
        }
        filterChain.doFilter(request, response);
    }
}
