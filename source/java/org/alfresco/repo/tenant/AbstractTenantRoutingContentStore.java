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
package org.alfresco.repo.tenant;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.content.AbstractRoutingContentStore;
import org.alfresco.repo.content.ContentContext;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.domain.tenant.TenantAdminDAO;
import org.alfresco.repo.domain.tenant.TenantEntity;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
    private static Log logger = LogFactory.getLog(AbstractTenantRoutingContentStore.class);
    
    private String defaultRootDirectory;
    private TenantAdminDAO tenantAdminDAO;
    protected TenantService tenantService;
    private ApplicationContext applicationContext;
    
    
    private final ReentrantReadWriteLock tenantContentStoreLock = new ReentrantReadWriteLock();
    private final WriteLock tenantContentStoreWriteLock = tenantContentStoreLock.writeLock();
    private final ReadLock tenantContentStoreReadLock = tenantContentStoreLock.readLock();
    
    // note: cache is tenant-aware (if using TransctionalCache impl)
    private SimpleCache<String, ContentStore> singletonCache; // eg. for contentStore
    private final String KEY_CONTENT_STORE = "key.tenant.routing.content.store";
    
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
    
    public void setSingletonCache(SimpleCache<String, ContentStore> singletonCache)
    {
        this.singletonCache = singletonCache;
    }
    
    /* (non-Javadoc)
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.
     * ApplicationContext)
     */
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.applicationContext = applicationContext;
    }
    
    @Override
    public String getRootLocation()
    {
        return defaultRootDirectory;
    }
    
    @Override
    protected ContentStore selectWriteStore(ContentContext ctx)
    {
        return getTenantContentStore();
    }
    
    @Override
    public List<ContentStore> getAllStores()
    {
        final List<ContentStore> allEnabledStores = new ArrayList<ContentStore>();
        
        if (tenantService.isEnabled())
        {
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
                
                if (logger.isDebugEnabled())
                {
                    logger.debug("getAllStores called without tenant ctx ("+tenants.size()+" tenants)");
                }
                
                // drop through to ensure default content store has been init'ed
            }
        }
        
        allEnabledStores.add(getTenantContentStore());
        
        return allEnabledStores;
    }
    
    private ContentStore getTenantContentStore()
    {
        ContentStore cs = null;
        
        tenantContentStoreReadLock.lock();
        try
        {
            cs = getTenantContentStoreImpl();
        }
        finally
        {
            tenantContentStoreReadLock.unlock();
        }
        
        if (cs == null)
        {
            synchronized (this) 
            {
                cs = getTenantContentStoreImpl();
                if (cs == null)
                {
                    init();
                    cs = getTenantContentStoreImpl();
                }
            }
        }
        return cs;
    }
    
    private ContentStore getTenantContentStoreImpl()
    {
        return (ContentStore)singletonCache.get(KEY_CONTENT_STORE);
    }
    
    private void putTenantContentStoreImpl(ContentStore contentStore)
    {
        singletonCache.put(KEY_CONTENT_STORE, contentStore);
    }
    
    private void removeTenantContentStoreImpl()
    {
        singletonCache.remove(KEY_CONTENT_STORE);
    }
    
    public void init()
    {
        tenantContentStoreWriteLock.lock();
        try
        {
            String rootDir = getRootLocation();
            Tenant tenant = tenantService.getTenant(tenantService.getCurrentUserDomain());
            if (tenant != null)
            {
                if (tenant.getRootContentStoreDir() != null)
                {
                   rootDir = tenant.getRootContentStoreDir();
                }
            }
            
            putTenantContentStoreImpl(initContentStore(this.applicationContext, rootDir));
        }
        finally
        {
            tenantContentStoreWriteLock.unlock();
        }
    }
    
    public void destroy()
    {
        tenantContentStoreWriteLock.lock();
        try
        {
            removeTenantContentStoreImpl();
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
    
    protected abstract ContentStore initContentStore(ApplicationContext ctx, String contentRoot);
}
