package org.alfresco.repo.domain.tenant;

import java.util.List;


/**
 * Data abstraction layer for Tenant entities.
 * 
 * @author janv
 * @since 4.0 (thor)
 */
public interface TenantAdminDAO
{
    /**
     * Create tenant - note: tenant domain must be unique
     */
    TenantEntity createTenant(TenantEntity tenantEntity);
    
    /**
     * Get tenant
     */
    TenantEntity getTenant(String tenantDomain);
    
    /**
     * List tenants
     */
    List<TenantEntity> listTenants(boolean enabledOnly);
    
    /**
     * Get tenant for update
     */
    TenantUpdateEntity getTenantForUpdate(String tenantDomain);
    
    /**
     * Update tenant
     * <p/>
     * Note: tenant domain cannot be changed
     */
    void updateTenant(TenantUpdateEntity tenantUpdateEntity);
    
    /**
     * Delete tenant
     */
    void deleteTenant(String tenantDomain);
}
