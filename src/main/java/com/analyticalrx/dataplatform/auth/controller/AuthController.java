package com.analyticalrx.dataplatform.auth.controller;

import java.time.Instant;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.analyticalrx.dataplatform.auth.model.LoginRequest;
import com.analyticalrx.dataplatform.auth.model.LoginResponse;
import com.analyticalrx.dataplatform.auth.model.PlatformUserDetails;
import com.analyticalrx.dataplatform.auth.service.JwtService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthenticationManager authenticationManager;
    private final JwtService jwt;
    public AuthController(AuthenticationManager authenticationManager, JwtService jwt) {
        this.authenticationManager = authenticationManager; this.jwt = jwt;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        System.out.println("Login request received for email: " + request.email());
        try {
            var authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));
            var user = (PlatformUserDetails) authentication.getPrincipal();
            var issuedAt = Instant.now();
            return ResponseEntity.ok(new LoginResponse(jwt.issue(user), "Bearer", 1800, issuedAt,
                    new LoginResponse.UserSummary(user.id(), user.email(), user.displayName(), user.status())));
        } catch (AuthenticationException ex) {
            log.warn("Authentication failed: email={}, reason={}", request.email(), ex.getClass().getSimpleName());
            return ResponseEntity.status(401).body(Map.of(
                    "error", "invalid_credentials",
                    "message", "Invalid email or password"));
        }
    }

    @PostMapping("/logout")
    public Map<String, String> logout(@AuthenticationPrincipal PlatformUserDetails ignored) {
        return Map.of("status", "logged_out");
    }
}
