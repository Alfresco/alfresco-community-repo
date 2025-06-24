/*
 * #%L
 * Alfresco Remote API
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
package org.alfresco.repo.web.scripts.tenant;

import org.springframework.extensions.webscripts.DeclarativeWebScript;

import org.alfresco.repo.tenant.TenantAdminService;

/**
 * @author janv
 * @since 4.2
 */
public abstract class AbstractTenantAdminWebScript extends DeclarativeWebScript
{
    protected static final String TENANT_DOMAIN = "tenantDomain";
    protected static final String TENANT_ADMIN_PASSWORD = "tenantAdminPassword";
    protected static final String TENANT_CONTENT_STORE_ROOT = "tenantContentStoreRoot";

    protected TenantAdminService tenantAdminService;

    public void setTenantAdminService(TenantAdminService tenantAdminService)
    {
        this.tenantAdminService = tenantAdminService;
    }
}
