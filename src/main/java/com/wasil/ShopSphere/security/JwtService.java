package com.wasil.ShopSphere.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms:3600000}")
    private long expirationMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );
    }

    public String generateToken(String email) {

        return Jwts.builder()
                .id(UUID.randomUUID().toString()) // jti — lets a single
                // token be individually
                // revoked (see logout)
                .subject(email)
                .issuedAt(new Date())
                .expiration(
                        new Date(System.currentTimeMillis() + expirationMs)
                )
                .signWith(getSigningKey())
                .compact();
    }

    public String extractUsername(String token) {

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /*
     * jti (JWT ID) — a unique identifier for this specific token, distinct
     * from the user's identity. This is what gets stored in the Redis
     * denylist on logout, so revoking one token doesn't affect any other
     * token the same user may have issued (e.g. from a different device).
     * Tokens issued before this claim existed will return null here —
     * callers must treat that as "cannot be individually revoked" rather
     * than an error.
     */
    public String extractJti(String token) {

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getId();
    }

    /*
     * Milliseconds until this token naturally expires. Used to set the
     * denylist entry's TTL on logout so Redis automatically cleans up the
     * entry the moment it would have stopped mattering anyway — we never
     * need to remember to delete it ourselves.
     */
    public long getRemainingValidityMillis(String token) {

        long remaining =
                extractExpiration(token).getTime() - System.currentTimeMillis();

        return Math.max(remaining, 0);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {

        String email = extractUsername(token);

        return email.equals(userDetails.getUsername())
                && !isTokenExpired(token);
    }
    private boolean isTokenExpired(String token) {

        return extractExpiration(token).before(new Date());
    }
    private Date extractExpiration(String token) {

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration();
    }
}