/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.tenant;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.alfresco.repo.content.AbstractRoutingContentStore;
import org.alfresco.repo.content.ContentContext;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.domain.tenant.TenantAdminDAO;
import org.alfresco.repo.domain.tenant.TenantEntity;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.Experimental;
import org.alfresco.service.transaction.TransactionService;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Content Store that supports tenant routing, if multi-tenancy is enabled.
 * 
 * Note: Need to initialise before the dictionary service, in the case that models are dynamically loaded for the tenant.
 */
public abstract class AbstractTenantRoutingContentStore extends AbstractRoutingContentStore implements ApplicationContextAware, TenantRoutingContentStore
{
    private String defaultRootDirectory;
    private TenantAdminDAO tenantAdminDAO;
    protected TenantService tenantService;
    private ApplicationContext applicationContext;
    private TransactionService transactionService;
    
    private final ReentrantReadWriteLock tenantContentStoreLock = new ReentrantReadWriteLock();
    private final WriteLock tenantContentStoreWriteLock = tenantContentStoreLock.writeLock();
    private final ReadLock tenantContentStoreReadLock = tenantContentStoreLock.readLock();
    
    // note: cache is tenant-aware (if using EhCacheAdapter shared cache)
    private final Map<String, ContentStore> cache;
    
    /**
     * Default constructor
     */
    protected AbstractTenantRoutingContentStore()
    {
        this.cache = new HashMap<String, ContentStore>(1024);
    }
    
    public void setRootLocation(String defaultRootDirectory)
    {
        this.defaultRootDirectory = defaultRootDirectory;
    }
    
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }
    
    public void setTenantAdminDAO(TenantAdminDAO tenantAdminDAO)
    {
        this.tenantAdminDAO = tenantAdminDAO;
    }
    
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.applicationContext = applicationContext;
    }
    
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    @Override
    public String getRootLocation()
    {
        return defaultRootDirectory;
    }
    
    @Override
    protected ContentStore selectWriteStore(ContentContext ctx)
    {
        RetryingTransactionCallback<ContentStore> callback = new RetryingTransactionCallback<ContentStore>()
        {
            @Override
            public ContentStore execute() throws Throwable
            {
                return getTenantContentStore();
            }
        };
        return transactionService.getRetryingTransactionHelper().doInTransaction(callback, true, false);
        
    }
    
    @Override
    public List<ContentStore> getAllStores()
    {
        RetryingTransactionCallback<List<ContentStore>> callback = new RetryingTransactionCallback<List<ContentStore>>()
        {
            @Override
            public List<ContentStore> execute() throws Throwable
            {
                return getAllStoresImpl();
            }
        };
        return transactionService.getRetryingTransactionHelper().doInTransaction(callback, true, false);
    }
    /**
     * Work method to build a list of stores and <b>must always be called from withing an active transaction</b>.
     */
    private List<ContentStore> getAllStoresImpl()
    {
        ContentStore cs = getTenantContentStore();

        // Bypass everything if MT is disabled
        if (!tenantService.isEnabled())
        {
            return Collections.singletonList(cs);
        }
        
        final List<ContentStore> allEnabledStores = new ArrayList<ContentStore>();
        allEnabledStores.add(cs);
        
        String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();
        if ((currentUser == null) || (tenantService.getBaseNameUser(currentUser).equals(AuthenticationUtil.getSystemUserName())))
        {
            // return enabled stores across all tenants, if running as system/null user, for example, ContentStoreCleaner scheduled job
            List<TenantEntity> tenants = tenantAdminDAO.listTenants(false);
            for (TenantEntity tenant : tenants)
            {
                if (tenant.getEnabled())
                {
                    String tenantDomain = tenant.getTenantDomain();
                    TenantUtil.runAsSystemTenant(new TenantRunAsWork<Void>()
                    {
                        public Void doWork() throws Exception
                        {
                            allEnabledStores.add(getTenantContentStore()); // note: cache should only contain enabled stores
                            return null;
                        }
                    }, tenantDomain);
                }
            }
        }
        return allEnabledStores;
    }
    
    private ContentStore getTenantContentStore()
    {
        String tenantDomain = tenantService.getCurrentUserDomain();
        ContentStore cs = null;
        
        tenantContentStoreReadLock.lock();
        try
        {
            cs = cache.get(tenantDomain);
            if (cs != null)
            {
                return cs;
            }
        }
        finally
        {
            tenantContentStoreReadLock.unlock();
        }
        
        // It was not found, so go and initialize it
        tenantContentStoreWriteLock.lock();
        try
        {
            String rootDir = getRootLocation();
            Tenant tenant = tenantService.getTenant(tenantDomain);
            if (tenant != null && tenant.getRootContentStoreDir() != null)
            {
                rootDir = tenant.getRootContentStoreDir();
            }
            cs = initContentStore(applicationContext, rootDir);
            cache.put(tenantDomain, cs);
            return cs;
        }
        finally
        {
            tenantContentStoreWriteLock.unlock();
        }
    }
    
    @Override
    public void init()
    {
        getTenantContentStore();
    }
    
    public void destroy()
    {
        String tenantDomain = tenantService.getCurrentUserDomain();
        tenantContentStoreWriteLock.lock();
        try
        {
            cache.remove(tenantDomain);
        }
        finally
        {
            tenantContentStoreWriteLock.unlock();
        }
    }
    
    public void onEnableTenant()
    {
        init();
    }
    
    public void onDisableTenant()
    {
        destroy();
    }
    
    @Override
    public long getSpaceFree()
    {
        ContentStore x = getTenantContentStore();
        if(x != null)
        {
            return x.getSpaceFree();
        }
        else
        {
            return -1;
        }
    }

    @Override
    public long getSpaceTotal()
    {
        ContentStore x = getTenantContentStore();
        if(x != null)
        {
            return x.getSpaceTotal();
        }
        else
        {
            return -1;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Experimental
    @Override
    public Map<String, String> getStorageProperties(String contentUrl)
    {
        return getTenantContentStore().getStorageProperties(contentUrl);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Experimental
    public boolean requestSendContentToArchive(String contentUrl, Map<String, Serializable> archiveParams)
    {
        return getTenantContentStore().requestSendContentToArchive(contentUrl, archiveParams);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Experimental
    public boolean requestRestoreContentFromArchive(String contentUrl, Map<String, Serializable> restoreParams)
    {
        return getTenantContentStore().requestRestoreContentFromArchive(contentUrl, restoreParams);
    }

    protected abstract ContentStore initContentStore(ApplicationContext ctx, String contentRoot);
}
