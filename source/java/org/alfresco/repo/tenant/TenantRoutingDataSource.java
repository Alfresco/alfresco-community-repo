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

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.extensions.surf.util.ParameterCheck;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * Experimental
 * 
 * @author janv
 * @since 4.2
 */
public class TenantRoutingDataSource extends AbstractRoutingDataSource 
{
    Map<String, DataSource> tenantDataSources = new HashMap<String, DataSource>();
    
    private BasicDataSource baseDataSource;
    
    private TenantService tenantService;
    
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }
    
    public void setBaseDataSource(BasicDataSource baseDataSource)
    {
        this.baseDataSource = baseDataSource;
    }
    
    @Override
    protected Object determineCurrentLookupKey() 
    {
        //return tenantService.getCurrentUserDomain(); // note: this is re-entrant if it checks whether tenant is enabled !
        String runAsUser = AuthenticationUtil.getRunAsUser();
        String tenantDomain = TenantService.DEFAULT_DOMAIN;
        if (runAsUser != null)
        {
            String[] parts = runAsUser.split(TenantService.SEPARATOR);
            if (parts.length == 2)
            {
                tenantDomain = parts[1];
            }
        }
        return tenantDomain;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void afterPropertiesSet()
    {
        ParameterCheck.mandatory("baseDataSource", baseDataSource);
        ParameterCheck.mandatory("tenantDataSources", tenantDataSources);
        
        String dbUrl = baseDataSource.getUrl();
        setTargetDataSources((Map)tenantDataSources);
        
        try
        {
            // default tenant
            DataSource defaultTargetDataSource = new TenantBasicDataSource(baseDataSource, dbUrl, -1);
            tenantDataSources.put(TenantService.DEFAULT_DOMAIN, defaultTargetDataSource);
            setDefaultTargetDataSource(defaultTargetDataSource);
        }
        catch (SQLException se)
        {
            throw new RuntimeException(se);
        }
        
       
        
        super.afterPropertiesSet();
    }
    
    public synchronized void addTenantDataSource(String tenantDomain, String dbUrl) throws SQLException
    {
        String currentTenantDomain = tenantService.getCurrentUserDomain();
        if (! TenantService.DEFAULT_DOMAIN.equals(currentTenantDomain))
        {
            throw new RuntimeException("Unexpected - should not be in context of a tenant ["+currentTenantDomain+"]");
        }
        
        tenantDataSources.put(tenantDomain, new TenantBasicDataSource(baseDataSource, dbUrl, -1));
        
        super.afterPropertiesSet(); // to update resolved data sources
    }
    
    public synchronized void removeTenantDataSource(String tenantDomain) throws SQLException
    {
        String currentTenantDomain = tenantService.getCurrentUserDomain();
        if (! TenantService.DEFAULT_DOMAIN.equals(currentTenantDomain))
        {
            throw new RuntimeException("Unexpected - should not be in context of a tenant ["+currentTenantDomain+"]");
        }
        
        tenantDataSources.remove(tenantDomain);
        
        super.afterPropertiesSet(); // to update resolved data sources
    }
}

