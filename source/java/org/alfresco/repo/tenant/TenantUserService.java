package org.alfresco.repo.tenant;

/**
 * Interface for Tenant User-Domain functionality.
 * 
 * @author janv
 * @author Derek Hulley
 * @since 3.0
 */
public interface TenantUserService
{
    public String getCurrentUserDomain();
    
    public String getDomain(String name);

    public String getUserDomain(String username);
    
    public String getDomainUser(String baseUsername, String tenantDomain);
    
    public boolean isEnabled();
}
