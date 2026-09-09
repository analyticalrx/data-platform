package com.analyticalrx.dataplatform.auth.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import com.analyticalrx.dataplatform.auth.model.PlatformUserDetails;

@Component
public class PermissionGuard {
    public void require(PlatformUserDetails user, String permission) {
        if (user == null || !user.permissions().contains(permission)) {
            throw new AccessDeniedException("Required permission is missing");
        }
    }
}
