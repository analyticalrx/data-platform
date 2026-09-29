package com.analyticalrx.dataplatform.auth.model;

import java.util.Collection;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public record PlatformUserDetails(
        long id,
        long tenantId,
        String email,
        String displayName,
        String passwordHash,
        String status,
        int permissionsVersion,
        Set<String> roles,
        Set<String> permissions,
        Collection<? extends GrantedAuthority> authorities
) implements UserDetails {
    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public String getPassword() { return passwordHash; }
    @Override public String getUsername() { return email; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return "active".equals(status); }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return "active".equals(status); }
}
