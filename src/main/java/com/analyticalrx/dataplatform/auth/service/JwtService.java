package com.analyticalrx.dataplatform.auth.service;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.analyticalrx.dataplatform.auth.model.PlatformUserDetails;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
    private final SecretKey signingKey;
    private final long expirationMillis;
    private final String issuer;
    private final String audience;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration-ms:1800000}") long expirationMillis,
                      @Value("${app.jwt.issuer:analyticalrx-api}") String issuer,
                      @Value("${app.jwt.audience:analyticalrx-frontend}") String audience) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expirationMillis = expirationMillis;
        this.issuer = issuer;
        this.audience = audience;
    }

    public String issue(PlatformUserDetails user) {
        Instant now = Instant.now();
        return Jwts.builder().subject(Long.toString(user.id()))
                .issuer(issuer).audience().add(audience).and()
                .claim("email", user.email()).claim("display_name", user.displayName())
                .claim("tenant_id", user.tenantId()).claim("roles", user.roles())
                .claim("permissions_version", user.permissionsVersion())
                .issuedAt(Date.from(now)).expiration(new Date(now.toEpochMilli() + expirationMillis))
                .id(UUID.randomUUID().toString()).signWith(signingKey).compact();
    }

    public long userId(String token) { return Long.parseLong(parse(token).getSubject()); }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(signingKey).requireIssuer(issuer).requireAudience(audience)
                .build().parseSignedClaims(token).getPayload();
    }
}
