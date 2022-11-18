/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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

import java.sql.SQLException;

import org.apache.commons.dbcp2.BasicDataSource;

/**
 * Experimental
 * 
 * @author janv
 * @since 4.2
 */
public class TenantBasicDataSource extends BasicDataSource
{
    public TenantBasicDataSource(BasicDataSource bds, String tenantUrl, int tenantMaxActive) throws SQLException
    {
        // tenant-specific
        this.setUrl(tenantUrl);
        this.setMaxTotal(tenantMaxActive == -1 ? bds.getMaxTotal() : tenantMaxActive);
        
        // defaults/overrides - see also 'baseDefaultDataSource' (core-services-context.xml + repository.properties)
        
        this.setDriverClassName(bds.getDriverClassName());
        this.setUsername(bds.getUsername());
        this.setPassword(bds.getPassword());
        
        this.setInitialSize(bds.getInitialSize());
        this.setMinIdle(bds.getMinIdle());
        this.setMaxIdle(bds.getMaxIdle());
        this.setDefaultAutoCommit(bds.getDefaultAutoCommit());
        this.setDefaultTransactionIsolation(bds.getDefaultTransactionIsolation());
        this.setMaxWaitMillis(bds.getMaxWaitMillis());
        this.setValidationQuery(bds.getValidationQuery());
        this.setTimeBetweenEvictionRunsMillis(bds.getTimeBetweenEvictionRunsMillis());
        this.setMinEvictableIdleTimeMillis(bds.getMinEvictableIdleTimeMillis());
        this.setNumTestsPerEvictionRun(bds.getNumTestsPerEvictionRun());
        this.setTestOnBorrow(bds.getTestOnBorrow());
        this.setTestOnReturn(bds.getTestOnReturn());
        this.setTestWhileIdle(bds.getTestWhileIdle());
        this.setRemoveAbandonedOnBorrow(bds.getRemoveAbandonedOnBorrow());
        this.setRemoveAbandonedTimeout(bds.getRemoveAbandonedTimeout());
        this.setPoolPreparedStatements(bds.isPoolPreparedStatements());
        this.setMaxOpenPreparedStatements(bds.getMaxOpenPreparedStatements());
        this.setLogAbandoned(bds.getLogAbandoned());
    }
}