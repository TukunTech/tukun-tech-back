package com.upc.tukuntech.backend.modules.auth.service;

import com.upc.tukuntech.backend.config.JwtProperties;
import com.upc.tukuntech.backend.modules.auth.entity.UserEntity;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    private final JwtProperties props;
    private final Key signingKey;

    public JwtService(JwtProperties props) {
        this.props = props;
        byte[] keyBytes = Decoders.BASE64.decode(props.getSecret());
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(UserEntity user) {
        Instant now = Instant.now();
        Instant expiry = now.plus(props.getAccessTokenExpiration());

        Map<String, Object> claims = new HashMap<>();
        claims.put("uid", user.getId());
        claims.put("email", user.getEmail());
        claims.put("roles", user.getRoles().stream().map(r -> r.getName()).toList());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail())
                .setIssuer(props.getIssuer())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getEmailFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .requireIssuer(props.getIssuer())
                .setAllowedClockSkewSeconds(60)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .requireIssuer(props.getIssuer())
                    .setAllowedClockSkewSeconds(60)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.JwtException e) {
            return false;
        }
    }

    public long getAccessTtlSeconds() {
        return props.getAccessTokenExpiration().toSeconds();
    }
}
