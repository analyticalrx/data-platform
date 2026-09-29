package com.analyticalrx.dataplatform.auth.controller;

import java.time.Instant;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.analyticalrx.dataplatform.auth.model.LoginRequest;
import com.analyticalrx.dataplatform.auth.model.LoginResponse;
import com.analyticalrx.dataplatform.auth.model.PlatformUserDetails;
import com.analyticalrx.dataplatform.auth.repository.AuthUserRepository;
import com.analyticalrx.dataplatform.auth.service.JwtService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final JwtService jwt;
    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;
    public AuthController(JwtService jwt, AuthUserRepository authUserRepository, PasswordEncoder passwordEncoder) {
        this.jwt = jwt;
        this.authUserRepository = authUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            var userOpt = authUserRepository.findByTenantKeyAndEmail(request.tenantKey(), request.email());
            if (userOpt.isEmpty()) {
                throw new BadCredentialsException("Invalid credentials");
            }

            var user = userOpt.get();

            var passwordMatch = passwordEncoder.matches(request.password(), user.getPassword());

            if (!passwordMatch){
                throw new BadCredentialsException("Invalid credentials");
            }

            if (user.roles().isEmpty()) {
                throw new BadCredentialsException("Invalid credentials");
            }
            var issuedAt = Instant.now();
            return ResponseEntity.ok(new LoginResponse(jwt.issue(user), "Bearer", 1800, issuedAt,
                    new LoginResponse.UserSummary(user.id(), user.email(), user.displayName(), user.status())));
        } catch (AuthenticationException ex) {
            log.warn("Authentication failed: reason={}", ex.getClass().getSimpleName());
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
