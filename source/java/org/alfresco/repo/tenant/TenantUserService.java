/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.tenant;

/**
 * Interface for Tenant User-Domain functionality.
 * 
 * @author janv
 * @author Derek Hulley
 * @since 3.0
 */
public interface TenantUserService
{
    /**
     * @return          the username <b>with</b> the tenant-specific ID attached
     */
    public String getDomainUser(String baseUsername, String tenantDomain);
    
    /**
     * @return          the username <b>without</b> the tenant-specific ID attached
     */
    public String getBaseNameUser(String name);
    
    /**
     * @return          the tenant-specific ID for current user
     */
    public String getCurrentUserDomain();

    /**
     * @return          the tenant-specific ID for specified username
     */
    public String getUserDomain(String username);
    
    /**
     * @return          the tenant-specific ID for specified identifier
     */
    public String getDomain(String name);
    
    /**
     * @return          true if the system is configured to be MT-enabled
     */
    public boolean isEnabled();
}
