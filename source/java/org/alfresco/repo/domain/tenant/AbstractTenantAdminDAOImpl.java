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
package org.alfresco.repo.domain.tenant;

import java.io.Serializable;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.cache.lookup.EntityLookupCache;
import org.alfresco.repo.cache.lookup.EntityLookupCache.EntityLookupCallbackDAO;
import org.alfresco.util.Pair;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.extensions.surf.util.ParameterCheck;


/**
 * Abstract implementation for TenantAdmin DAO.
 * <p>
 * This provides basic services such as caching, but defers to the underlying implementation
 * for CRUD operations for:
 * 
 *     <b>alf_tenant</b>
 *     
 * @author janv
 * @since 4.0 (thor)
 */
public abstract class AbstractTenantAdminDAOImpl implements TenantAdminDAO
{
    private final TenantEntityCallbackDAO tenantEntityDaoCallback;
    
    /**
     * Cache for the Tenant entity:<br/>
     * KEY: TenantDomain (String)<br/>
     * VALUE: TenantEntity<br/>
     * VALUE KEY: None<br/>
     */
    private EntityLookupCache<String, TenantEntity, Serializable> tenantEntityCache;
    
    /**
     * Set the cache to use for <b>alf_tenant</b> lookups (optional).
     * 
     * @param tenantEntityCache      the cache of tenantDomains to TenantEntities
     */
    public void setTenantEntityCache(SimpleCache<Serializable, Object> tenantEntityCache)
    {
        this.tenantEntityCache = new EntityLookupCache<String, TenantEntity, Serializable>(
                tenantEntityCache,
                tenantEntityDaoCallback);
    }
    
    /**
     * Default constructor.
     * <p>
     * This sets up the DAO accessor to bypass any caching to handle the case where the caches are not
     * supplied in the setters.
     */
    public AbstractTenantAdminDAOImpl()
    {
        this.tenantEntityDaoCallback = new TenantEntityCallbackDAO();
        this.tenantEntityCache = new EntityLookupCache<String, TenantEntity, Serializable>(tenantEntityDaoCallback);
    }
    
    @Override
    public TenantEntity createTenant(TenantEntity entity)
    {
        ParameterCheck.mandatory("entity", entity);
        ParameterCheck.mandatoryString("entity.tenantDomain", entity.getTenantDomain());
        
        if (entity.getEnabled() == null)
        {
            entity.setEnabled(true);
        }
        
        // force lower-case on create
        entity.setTenantDomain(entity.getTenantDomain().toLowerCase());
        
        entity.setVersion(0L);
        
        Pair<String, TenantEntity> entityPair = tenantEntityCache.getOrCreateByValue(entity);
        return entityPair.getSecond();
    }
    
    @Override
    public TenantEntity getTenant(String tenantDomain)
    {
        return getTenantImpl(tenantDomain);
    }
    
    private TenantEntity getTenantImpl(String tenantDomain)
    {
        tenantDomain = tenantDomain.toLowerCase();
        Pair<String, TenantEntity> entityPair = tenantEntityCache.getByKey(tenantDomain);
        if (entityPair == null)
        {
            // try lower-case to make sure
            entityPair = tenantEntityCache.getByKey(tenantDomain);
            if (entityPair == null)
            {
                return null;
            }
        }
        return entityPair.getSecond();
    }
    
    @Override
    public List<TenantEntity> listTenants(boolean enabledOnly)
    {
        if (enabledOnly)
        {
            return getTenantEntities(Boolean.TRUE);
        }
        else
        {
            return getTenantEntities(null);
        }
    }
    
    @Override
    public TenantUpdateEntity getTenantForUpdate(String tenantDomain)
    {
        TenantEntity entity = getTenantImpl(tenantDomain);
        if (entity == null)
        {
            return null;
        }
        
        // copy for update
        TenantUpdateEntity updateEntity = new TenantUpdateEntity(entity.getTenantDomain());
        updateEntity.setVersion(entity.getVersion());
        updateEntity.setEnabled(entity.getEnabled());
        updateEntity.setContentRoot(entity.getContentRoot());
        updateEntity.setDbUrl(entity.getDbUrl());
        updateEntity.setTenantName(entity.getTenantName());
        
        return updateEntity;
    }
    
    @Override
    public void updateTenant(TenantUpdateEntity entity)
    {
        ParameterCheck.mandatory("entity", entity);
        ParameterCheck.mandatory("entity.version", entity.getVersion());
        ParameterCheck.mandatoryString("entity.tenantDomain", entity.getTenantDomain());
        
        int updated = tenantEntityCache.updateValue(entity.getTenantDomain(), entity);
        if (updated < 1)
        {
            throw new ConcurrencyFailureException("TenantEntity " + entity.getTenantDomain() + " no longer exists or has been updated concurrently");
        }
    }
    
    @Override
    public void deleteTenant(String tenantDomain)
    {
        ParameterCheck.mandatoryString("tenantDomain", tenantDomain);
        
        // force lower-case on delete
        tenantDomain = tenantDomain.toLowerCase();
        
        int deleted = tenantEntityCache.deleteByKey(tenantDomain);
        if (deleted < 1)
        {
            throw new ConcurrencyFailureException("TenantEntity " + tenantDomain + " no longer exists");
        }
    }
    
    /**
     * Callback for <b>alf_tenant</b> DAO
     */
    private class TenantEntityCallbackDAO implements EntityLookupCallbackDAO<String, TenantEntity, Serializable>
    {
        private final Pair<String, TenantEntity> convertEntityToPair(TenantEntity entity)
        {
            if (entity == null)
            {
                return null;
            }
            else
            {
                return new Pair<String, TenantEntity>(entity.getTenantDomain(), entity);
            }
        }
        
        @Override
        public Serializable getValueKey(TenantEntity value)
        {
            return null;
        }
        
        @Override
        public Pair<String, TenantEntity> createValue(TenantEntity value)
        {
            TenantEntity entity = createTenantEntity(value);
            return convertEntityToPair(entity);
        }
        
        @Override
        public Pair<String, TenantEntity> findByKey(String key)
        {
            TenantEntity entity = getTenantEntity(key);
            return convertEntityToPair(entity);
        }
        
        @Override
        public Pair<String, TenantEntity> findByValue(TenantEntity value)
        {
            if ((value == null) || (value.getTenantDomain() == null))
            {
                throw new AlfrescoRuntimeException("Unexpected: TenantEntity / tenantDomain must not be null");
            }
            return convertEntityToPair(getTenantEntity(value.getTenantDomain()));
        }
        
        @Override
        public int updateValue(String tenantDomain, TenantEntity value)
        {
            return updateTenantEntity(value);
        }
        
        @Override
        public int deleteByKey(String tenantDomain)
        {
            return deleteTenantEntity(tenantDomain);
        }
        
        @Override
        public int deleteByValue(TenantEntity value)
        {
            throw new UnsupportedOperationException("deleteByValue");
        }
    }
    
    protected abstract TenantEntity createTenantEntity(TenantEntity tenantEntity);
    protected abstract TenantEntity getTenantEntity(String tenantDomain);
    /**
     * @param enabled       Enabled or disabled tenants or <tt>null</tt> for no filter
     */
    protected abstract List<TenantEntity> getTenantEntities(Boolean enabled);
    protected abstract int updateTenantEntity(TenantEntity tenantEntity);
    protected abstract int deleteTenantEntity(String tenantDomain);
}
