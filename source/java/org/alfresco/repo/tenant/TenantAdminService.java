/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
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
    
    public List<Tenant> getAllTenants();
    
    /*
     * Workflow Deployer methods
     */

    public void register(WorkflowDeployer workflowDeployer);
    
    /*
     * Admin methods
     */
    
    public void createTenant(String tenantDomain, char[] adminRawPassword);

    public void createTenant(String tenantDomain, char[] adminRawPassword, String rootContentStoreDir);
    
    public void exportTenant(String tenantDomain, File directoryDestination);
    
    public void importTenant(String tenantDomain, File directorySource, String rootContentStoreDir);
    
    public boolean existsTenant(String tenantDomain);
    
    public void deleteTenant(String tenantDomain);
    
    public void enableTenant(String tenantDomain);
    
    public void disableTenant(String tenantDomain);
    
    public Tenant getTenant(String tenantDomain);
    
    public boolean isEnabledTenant(String tenantDomain);
}
