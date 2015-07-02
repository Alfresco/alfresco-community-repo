package org.alfresco.repo.web.auth;

public interface TenantAuthentication
{
    /**
     * Authenticate user against tenant
     * 
     * @param username String
     * @param tenant String
     * @return  true => authenticated, false => not authenticated
     */
    boolean authenticateTenant(String username, String tenant);
}
