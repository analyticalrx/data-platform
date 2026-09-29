package com.analyticalrx.dataplatform.auth.service;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.analyticalrx.dataplatform.auth.model.PlatformUserDetails;
import com.analyticalrx.dataplatform.auth.repository.AuthUserRepository;

@Service
public class PlatformUserDetailsService {
    private final AuthUserRepository users;
    public PlatformUserDetailsService(AuthUserRepository users) { this.users = users; }

    public PlatformUserDetails loadById(long id) {
        return users.findById(id).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
