/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.repo.web.scripts;

import org.alfresco.repo.cache.AsynchronouslyRefreshedCache;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.tenant.TenantDeployer;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Registry;

/**
 * Tenant-aware Repository (server-tier) container for Web Scripts
 * 
 * @author davidc
 */
public class TenantRepositoryContainer extends RepositoryContainer implements TenantDeployer
{
    // Logger
    protected static final Log logger = LogFactory.getLog(TenantRepositoryContainer.class);

    /* Component Dependencies */
    protected TenantAdminService tenantAdminService;
    protected TransactionService transactionService;
    private AsynchronouslyRefreshedCache<Registry> registryCache;

    /**
     * @param registryCache                 asynchronously maintained cache for script registries
     */
    public void setWebScriptsRegistryCache(AsynchronouslyRefreshedCache<Registry> registryCache)
    {
        this.registryCache = registryCache;
    }
    
    /**
     * @param tenantAdminService            service to sort out tenant context
     */
    public void setTenantAdminService(TenantAdminService tenantAdminService)
    {
        this.tenantAdminService = tenantAdminService;
    }
    
    /**
     * @param transactionService            service to give transactions when reading from the container
     */
    public void setTransactionService(TransactionService transactionService)
    {
        super.setTransactionService(transactionService);
        this.transactionService = transactionService;
    }

    @Override
    public Registry getRegistry()
    {
        Registry registry = registryCache.get();
        boolean isUpToDate = registryCache.isUpToDate();
        if (!isUpToDate && logger.isDebugEnabled())
        {
            logger.debug("Retrieved out of date web script registry for tenant " + tenantAdminService.getCurrentUserDomain());
        }
        return registry;
    }
    
    @Override
    public void onEnableTenant()
    {
        init();
    }
    
    @Override
    public void onDisableTenant()
    {
        destroy();
    }
    
    @Override
    public void init()
    {
        tenantAdminService.register(this);
        registryCache.refresh();
        
        super.reset();
    }
    
    @Override
    public void destroy()
    {
        registryCache.refresh();
    }
    
    @Override
    public void reset()
    {
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Exception
            {
                destroy();
                init();

                return null;
            }
        }, true, false);
    }
}
