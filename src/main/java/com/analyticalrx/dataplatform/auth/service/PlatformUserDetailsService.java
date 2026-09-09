package com.analyticalrx.dataplatform.auth.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.analyticalrx.dataplatform.auth.model.PlatformUserDetails;
import com.analyticalrx.dataplatform.auth.repository.AuthUserRepository;

@Service
public class PlatformUserDetailsService implements UserDetailsService {
    private static final Logger log = LoggerFactory.getLogger(PlatformUserDetailsService.class);
    private final AuthUserRepository users;
    public PlatformUserDetailsService(AuthUserRepository users) { this.users = users; }
    @Override public UserDetails loadUserByUsername(String email) {
        var user = users.findByEmail(email);
        if (user.isEmpty()) {
            log.warn("Authentication user lookup failed: email={}", email);
            throw new UsernameNotFoundException("User not found");
        }
        var details = user.get();
        log.info("Authentication user found: id={}, email={}, tenantId={}, status={}, roles={}",
                details.id(), details.email(), details.tenantId(), details.status(), details.roles());
        return details;
    }
    public PlatformUserDetails loadById(long id) {
        return users.findById(id).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
