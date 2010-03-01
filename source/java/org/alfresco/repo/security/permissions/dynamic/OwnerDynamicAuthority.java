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
package org.alfresco.repo.security.permissions.dynamic;

import java.util.Set;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.permissions.DynamicAuthority;
import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.util.EqualsHelper;
import org.springframework.beans.factory.InitializingBean;

/**
 * The owner dynamic authority
 * @author andyh
 *
 */
public class OwnerDynamicAuthority implements DynamicAuthority, InitializingBean
{
    private OwnableService ownableService;

    /**
     * Standard construction
     */
    public OwnerDynamicAuthority()
    {
        super();
    }

    /**
     * Set the ownable service
     * @param ownableService
     */
    public void setOwnableService(OwnableService ownableService)
    {
        this.ownableService = ownableService;
    }

    public void afterPropertiesSet() throws Exception
    {
        if (ownableService == null)
        {
            throw new IllegalArgumentException("There must be an ownable service");
        }
    }

    public boolean hasAuthority(final NodeRef nodeRef, final String userName)
    {
        return AuthenticationUtil.runAs(new RunAsWork<Boolean>(){

            public Boolean doWork() throws Exception
            {
                // TODO Auto-generated method stub
                return EqualsHelper.nullSafeEquals(ownableService.getOwner(nodeRef), userName);
            }}, AuthenticationUtil.getSystemUserName());
       
    }

    public String getAuthority()
    {
       return PermissionService.OWNER_AUTHORITY;
    }

    public Set<PermissionReference> requiredFor()
    {
        return null;
    }

}
