package org.alfresco.repo.tenant;

import java.io.File;
import java.util.List;

import org.alfresco.repo.workflow.WorkflowDeployer;
import org.apache.commons.logging.Log;


/**
 * Tenant Admin Service interface.
 * <p>
 * This interface provides administrative methods to provision and administer tenants.
 *
 */

public interface TenantAdminService extends TenantUserService
{
    public void startTenants();
    
    public void stopTenants();
    
    /*
     * Tenant Deployer methods
     */

    public void deployTenants(final TenantDeployer deployer, Log logger);
    
    public void undeployTenants(final TenantDeployer deployer, Log logger);

    public void register(TenantDeployer tenantDeployer);
    
    public void unregister(TenantDeployer tenantDeployer);
    
    /**
     * @return          a list of <b>all</b> tenants regardless of state
     * 
     * @deprecated      Deprecated in <b>4.2</b>.  This method does not scale.
     */
    @Deprecated
    public List<Tenant> getAllTenants();
    
    /**
     * Retrieve all tenants
     * 
     * @param enabledOnly   <tt>true</tt> to retrieve only active tenants
     * @return              tenants, either active or all
     *
     * @since               4.2
     * @deprecated          method does not scale.
     */
    @Deprecated
    public List<Tenant> getTenants(boolean enabledOnly);
    
    /*
     * Workflow Deployer methods
     */

    public void register(WorkflowDeployer workflowDeployer);
    
    /*
     * Admin methods
     */
    
    public void createTenant(String tenantDomain, char[] adminRawPassword);
    
    public void createTenant(String tenantDomain, char[] adminRawPassword, String contentRoot);

    // experimental (unsupported)
    public void createTenant(String tenantDomain, char[] adminRawPassword, String contentRoot, String dbUrl);
    
    public void exportTenant(String tenantDomain, File directoryDestination);
    
    public void importTenant(String tenantDomain, File directorySource, String contentRoot);
    
    public boolean existsTenant(String tenantDomain);
    
    public void deleteTenant(String tenantDomain);
    
    public void enableTenant(String tenantDomain);
    
    public void disableTenant(String tenantDomain);
    
    public Tenant getTenant(String tenantDomain);
    
    public boolean isEnabledTenant(String tenantDomain);
}
