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
package org.alfresco.repo.tenant;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Thrown when an attempt is made to use a disabled tenant.
 * 
 * @author Matt Ward
 */
public class TenantDisabledException extends AlfrescoRuntimeException
{
    public static final String DISABLED_TENANT_MSG = "system.mt.disabled";
    private static final long serialVersionUID = 1L;
    private String tenantDomain;
    
    public TenantDisabledException(String tenantDomain)
    {
        super(DISABLED_TENANT_MSG, new Object[] { tenantDomain });
        this.tenantDomain = tenantDomain;
    }

    /**
     * @return the tenantDomain
     */
    public String getTenantDomain()
    {
        return tenantDomain;
    }
}
