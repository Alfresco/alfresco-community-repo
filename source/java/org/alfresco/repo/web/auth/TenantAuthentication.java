package org.alfresco.repo.web.auth;

public interface TenantAuthentication
{
    /**
     * Authenticate user against tenant
     * 
     * @param email
     * @param tenant
     * @return  true => authenticated, false => not authenticated
     */
    boolean authenticateTenant(String username, String tenant);
}
