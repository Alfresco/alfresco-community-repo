package org.alfresco.repo.tenant;

/**
 * Interface for Tenant User-Domain functionality.
 * 
 * @author Jan Vonka
 * @author Derek Hulley
 * @since 3.0
 */
public interface TenantUserService
{
    public String getCurrentUserDomain();
    
    public String getDomainUser(String baseUsername, String tenantDomain);
    
    public String getDomain(String name);
 
    public boolean isEnabled();
}
