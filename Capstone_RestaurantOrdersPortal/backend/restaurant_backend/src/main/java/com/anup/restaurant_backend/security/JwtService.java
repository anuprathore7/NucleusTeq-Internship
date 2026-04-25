package com.anup.restaurant_backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

/**
 *  This class handles:
 * → JWT creation
 * → extracting username
 */
@Service
public class JwtService {

    private final String SECRET = "mysecretkeymysecretkeymysecretkey123";

    private Key getSignKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    /**
     *  Generate token using email
     */
    public String generateToken(String email, String role) {  // ← add role param
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)           // ← ADD THIS LINE
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(getSignKey())
                .compact();
    }

    /**
     *  Extract email from token
     */
    public String extractEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }




}