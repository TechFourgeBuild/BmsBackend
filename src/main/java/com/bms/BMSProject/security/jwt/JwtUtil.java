package com.bms.BMSProject.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expirationMs;

    @Value("${jwt.secret.refreshToken}")
    private String refreshTokenSecretKey;

    @Value("${jwt.refresh.expiration}")
    private long refreshTokenExpiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private SecretKey getRefreshSigninKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshTokenSecretKey));
    }

    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    public Date convertToDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }


    public String generateRefreshToken(String username, LocalDateTime expiryDate) {
         return Jwts.builder()
                 .subject(username)
                 .claim("type","refresh")
                 .issuedAt(new Date())
                 .expiration(convertToDate(expiryDate))
                 .signWith(getRefreshSigninKey())
                 .compact();
    }

    public String extractUsername(String token, boolean isRefreshToken) {
        return extractClaims(token, isRefreshToken).getSubject();
    }

    public boolean isTokenExpired(String token, boolean isRefreshToken) {
        return extractClaims(token,isRefreshToken).getExpiration().before(new Date());
    }

    public boolean validateAccessToken(String token, UserDetails userDetails) {
        try {
            return extractUsername(token,false).equals(userDetails.getUsername()) && !isTokenExpired(token,false);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean validateRefreshToken(String token) {
        try {
            extractUsername(token,true);
            return !isTokenExpired(token,true);
        } catch (Exception e) {
            return false;
        }
    }

    private Claims extractClaims(String token, boolean isRefreshToken) {
        SecretKey key = !isRefreshToken ? getSigningKey():getRefreshSigninKey();
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}