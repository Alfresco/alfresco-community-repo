/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
import java.util.Arrays;
import java.util.List;

import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.content.AbstractRoutingContentStore;
import org.alfresco.repo.content.ContentContext;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.domain.tenant.TenantAdminDAO;
import org.alfresco.repo.domain.tenant.TenantEntity;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
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
    private TenantService tenantService;
    private ApplicationContext applicationContext;
    
    // note: cache is tenant-aware (if using EhCacheAdapter shared cache)
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
        if (tenantService.isEnabled())
        {
            String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();
            if ((currentUser == null) || (tenantService.getBaseNameUser(currentUser).equals(AuthenticationUtil.getSystemUserName())))
            {
                // return enabled stores across all tenants, if running as system/null user, for example, ContentStoreCleaner scheduled job
                final List<ContentStore> allEnabledStores = new ArrayList<ContentStore>();
                
                List<TenantEntity> tenants = tenantAdminDAO.listTenants();
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
                
                if (allEnabledStores.size() > 0)
                {
                    allEnabledStores.add(getTenantContentStore());
                    return allEnabledStores;
                }
                
                // drop through to ensure default content store has been init'ed
            }
        }
        return Arrays.asList(getTenantContentStore());
    }
    
    private ContentStore getTenantContentStore()
    {
        ContentStore cs = (ContentStore)singletonCache.get(KEY_CONTENT_STORE);
        if (cs == null)
        {
            init();
            cs = (ContentStore)singletonCache.get(KEY_CONTENT_STORE);
        }
        return cs;
    }
    
    private void putTenantContentStore(ContentStore contentStore)
    {
        singletonCache.put(KEY_CONTENT_STORE, contentStore);
    }
    
    private void removeTenantContentStore()
    {
        singletonCache.remove(KEY_CONTENT_STORE);
    }
    
    public void init()
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
        
        putTenantContentStore(initContentStore(this.applicationContext, rootDir));
    }
    
    public void destroy()
    {
        removeTenantContentStore();
    }
    
    public void onEnableTenant()
    {
        init();
    }
    
    public void onDisableTenant()
    {
        destroy();
    }
    
    protected abstract ContentStore initContentStore(ApplicationContext ctx, String contentRoot);
}
