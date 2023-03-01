package com.ncr.sv.authenticationservice.multitenancy;

/**
 * Description: Class used to set tenant value to TenantContext
 *
 */
public class TenantContext {

    private static ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    public static String getCurrentTenant() {
        return CURRENT_TENANT.get();
    }

    public static void setCurrentTenant(String tenant) {
        CURRENT_TENANT.set(tenant);
    }
}
