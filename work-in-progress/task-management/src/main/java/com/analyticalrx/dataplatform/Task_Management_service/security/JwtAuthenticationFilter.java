package com.primerx.task.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.HashSet;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(key)
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

                String userId = claims.getSubject();
                String tenantId = claims.get("tenantId", String.class);
                String pharmacyKey = claims.get("pharmacyKey", String.class);

                @SuppressWarnings("unchecked")
                List<String> rolesList = claims.get("roles", List.class);
                HashSet<String> roles = rolesList != null ? new HashSet<>(rolesList) : new HashSet<>();

                UserContext userContext = new UserContext(userId, tenantId, pharmacyKey, roles, new HashSet<>());
                UserContext.setContext(userContext);

            } catch (Exception e) {
                logger.error("Could not set user authentication in security context", e);
            }
        } else {
            // Fallback: Check custom HTTP headers for local testing
            String tenantId = request.getHeader("X-Tenant-ID");
            String userId = request.getHeader("X-User-ID");
            String pharmacyKey = request.getHeader("X-Pharmacy-Key");

            if (tenantId == null || tenantId.isBlank()) tenantId = "TENANT_001";
            if (userId == null || userId.isBlank()) userId = "USER_123";
            if (pharmacyKey == null || pharmacyKey.isBlank()) pharmacyKey = "PHARM_999";

            UserContext userContext = new UserContext(userId, tenantId, pharmacyKey);
            UserContext.setContext(userContext);
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            UserContext.clear();
        }
    }
}