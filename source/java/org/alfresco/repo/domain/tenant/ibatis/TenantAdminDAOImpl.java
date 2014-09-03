/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.domain.tenant.ibatis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.domain.tenant.AbstractTenantAdminDAOImpl;
import org.alfresco.repo.domain.tenant.TenantEntity;
import org.alfresco.repo.domain.tenant.TenantQueryEntity;
import org.mybatis.spring.SqlSessionTemplate;

/**
 * iBatis-specific implementation of the TenantAdmin DAO.
 * 
 * @author janv
 * @since 4.0 (thor)
 */
public class TenantAdminDAOImpl extends AbstractTenantAdminDAOImpl
{
    private static final String INSERT_TENANT = "alfresco.tenants.insert_Tenant";
    private static final String SELECT_TENANT = "alfresco.tenants.select_Tenant";
    private static final String SELECT_TENANTS = "alfresco.tenants.select_Tenants";
    private static final String UPDATE_TENANT = "alfresco.tenants.update_Tenant";
    private static final String DELETE_TENANT = "alfresco.tenants.delete_Tenant";
    
    private SqlSessionTemplate template;
    
    public final void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) 
    {
        this.template = sqlSessionTemplate;
    }
    
    
    @Override
    protected TenantEntity createTenantEntity(TenantEntity entity)
    {
        entity.setVersion(0L);
        template.insert(INSERT_TENANT, entity);
        return entity;
    }
    
    @Override
    protected TenantEntity getTenantEntity(String tenantDomain)
    {
        Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("tenantDomain", tenantDomain);
        
        return template.selectOne(SELECT_TENANT, params);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected List<TenantEntity> getTenantEntities(Boolean enabled)
    {
        TenantQueryEntity entity = new TenantQueryEntity();
        entity.setEnabled(enabled);
        return template.selectList(SELECT_TENANTS, entity);
    }
    
    @Override
    protected int updateTenantEntity(TenantEntity tenantEntity)
    {
        tenantEntity.incrementVersion();
        
        return template.update(UPDATE_TENANT, tenantEntity);
    }
    
    @Override
    protected int deleteTenantEntity(String tenantDomain)
    {
        Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("tenantDomain", tenantDomain);
        
        return template.delete(DELETE_TENANT, params);
    }
}
