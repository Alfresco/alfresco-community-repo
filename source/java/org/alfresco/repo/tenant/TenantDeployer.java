/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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


/**
 * Tenant Deployer interface.
 * <p>
 * This interface allows components to be notified of tenant events.
 * Components will register with TenantAdminService.
 * Also callbacks used during bootstrap (init) and shutdown (destroy)
 *
 */

public interface TenantDeployer
{    
    public void onEnableTenant();
    
    public void onDisableTenant();
    
    // callback for bootstrap (for each tenant)
    public void init();
    
    // callback for shutdown (for each tenant)
    public void destroy();
}
