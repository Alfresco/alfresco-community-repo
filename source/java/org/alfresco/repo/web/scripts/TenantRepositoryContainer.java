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

import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.tenant.TenantDeployer;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.ObjectFactory;
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

    /** Component Dependencies */
    private TenantAdminService tenantAdminService;
    private TransactionService transactionService;
    private ObjectFactory registryFactory;
    private SimpleCache<String, Registry> webScriptsRegistryCache;
    private boolean initialized;

    /**
     * @param webScriptsRegistryCache
     */
    public void setWebScriptsRegistryCache(SimpleCache<String, Registry> webScriptsRegistryCache)
    {
        this.webScriptsRegistryCache = webScriptsRegistryCache;
    }
    
    /**
     * @param registryFactory
     */
    public void setRegistryFactory(ObjectFactory registryFactory)
    {
        this.registryFactory = registryFactory;
    }
    
    /**
     * @param tenantAdminService
     */
    public void setTenantAdminService(TenantAdminService tenantAdminService)
    {
        this.tenantAdminService = tenantAdminService;
    }
    
    /**
     * @param transactionService the transactionService to set
     */
    public void setTransactionService(TransactionService transactionService)
    {
        super.setTransactionService(transactionService);
        this.transactionService = transactionService;
    }

    
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.AbstractRuntimeContainer#getRegistry()
     */
    @Override
    public Registry getRegistry()
    {
        String tenantDomain = tenantAdminService.getCurrentUserDomain();
        Registry registry = webScriptsRegistryCache.get(tenantDomain);
        if (registry == null)
        {
            registry = (Registry)registryFactory.getObject();
            // We only need to reset the registry if the superclass thinks its already initialized
            if (initialized)
            {
                registry.reset();
            }
            webScriptsRegistryCache.put(tenantDomain, registry);
        }
        return registry;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.tenant.TenantDeployer#onEnableTenant()
     */
    public void onEnableTenant()
    {
        init();
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.tenant.TenantDeployer#onDisableTenant()
     */
    public void onDisableTenant()
    {
        destroy();
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.tenant.TenantDeployer#init()
     */
    public void init()
    {
        tenantAdminService.register(this);
        
        super.reset();
        
        initialized = true;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.tenant.TenantDeployer#destroy()
     */
    public void destroy()
    {
        webScriptsRegistryCache.remove(tenantAdminService.getCurrentUserDomain());
        
        initialized = false;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.AbstractRuntimeContainer#reset()
     */
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
