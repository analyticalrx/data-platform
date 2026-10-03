package com.primerx.task.security;

import java.util.Set;

public class UserContext {

    private String userId;
    private String tenantId;
    private String pharmacyKey;
    private Set<String> roles;
    private Set<String> permissions;

    private static final ThreadLocal<UserContext> CONTEXT = new ThreadLocal<>();

    public UserContext() {
    }

    public UserContext(String userId, String tenantId, String pharmacyKey) {
        this.userId = userId;
        this.tenantId = tenantId;
        this.pharmacyKey = pharmacyKey;
    }

    public UserContext(String userId, String tenantId, String pharmacyKey, Set<String> roles, Set<String> permissions) {
        this.userId = userId;
        this.tenantId = tenantId;
        this.pharmacyKey = pharmacyKey;
        this.roles = roles;
        this.permissions = permissions;
    }

    // Static ThreadLocal Management Methods
    public static void setContext(UserContext context) {
        CONTEXT.set(context);
    }

    public static UserContext getContext() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getPharmacyKey() { return pharmacyKey; }
    public void setPharmacyKey(String pharmacyKey) { this.pharmacyKey = pharmacyKey; }

    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }

    public Set<String> getPermissions() { return permissions; }
    public void setPermissions(Set<String> permissions) { this.permissions = permissions; }
}