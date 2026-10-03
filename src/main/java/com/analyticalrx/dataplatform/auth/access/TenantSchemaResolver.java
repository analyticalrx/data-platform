package com.analyticalrx.dataplatform.auth.access;

import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import com.analyticalrx.dataplatform.auth.model.PlatformUserDetails;
import com.analyticalrx.dataplatform.auth.repository.TenantRegistryRepository;

@Component 
public class TenantSchemaResolver {
    private final TenantRegistryRepository tenantRegistryRepository;

    public TenantSchemaResolver(TenantRegistryRepository tenantRegistryRepository){
        this.tenantRegistryRepository = tenantRegistryRepository;
    }

    public String findActiveSchemaName(PlatformUserDetails userDetails){

        if (userDetails == null || userDetails.tenantId() <= 0){
            throw new AccessDeniedException("Tenant is not valid");
        }

        Optional<String> tenantNameOpt = tenantRegistryRepository.findActiveSchemaName(userDetails.tenantId());
        if (tenantNameOpt.isEmpty()){
            throw new AccessDeniedException("Tenant is not valid");
        }

        String tenantName = tenantNameOpt.get();
        if (!Pattern.matches( "^tenant_[a-z0-9_]+$", tenantName)){
            throw new IllegalStateException("Invalid tenant details");
        }
        return tenantName;
        
    }
}
