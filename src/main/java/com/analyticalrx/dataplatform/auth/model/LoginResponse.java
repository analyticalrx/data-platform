package com.analyticalrx.dataplatform.auth.model;

import java.time.Instant;

public record LoginResponse(String accessToken, String tokenType, long expiresInSeconds,
                            Instant issuedAt, UserSummary user) {
    public record UserSummary(long id, String email, String displayName, String status) {}
}
