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
package org.alfresco.opencmis;

import org.alfresco.repo.tenant.NetworksService;
import org.alfresco.repo.tenant.TenantAdminService;

/**
 * Override factory for OpenCMIS service objects - for public api
 * 
 * @author steveglover
 * @author janv
 * @since PublicApi1.0
 */
public class PublicApiAlfrescoCmisServiceFactory extends AlfrescoCmisServiceFactory
{
    private TenantAdminService tenantAdminService;
    private NetworksService networksService;

    public void setNetworksService(NetworksService networksService)
    {
		this.networksService = networksService;
	}

	public void setTenantAdminService(TenantAdminService tenantAdminService)
    {
        this.tenantAdminService = tenantAdminService;
    }
    
    @Override
    protected AlfrescoCmisService getCmisServiceTarget(CMISConnector connector)
    {
        return new PublicApiAlfrescoCmisService(connector, tenantAdminService, networksService);
    }
}
