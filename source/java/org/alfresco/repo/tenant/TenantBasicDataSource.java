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

import org.apache.commons.dbcp.BasicDataSource;

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
        this.setMaxActive(tenantMaxActive == -1 ? bds.getMaxActive() : tenantMaxActive);
        
        // defaults
        this.setUsername(bds.getUsername());
        this.setPassword(bds.getPassword());
        this.setDriverClassName(bds.getDriverClassName());
        
        this.setMaxIdle(bds.getMaxIdle());
        this.setMinIdle(bds.getMinIdle());
        
        // TODO other default settings
    }
}