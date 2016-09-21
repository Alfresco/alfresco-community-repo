/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.util;

import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;

/**
 * Helper bean to allow injection of AuthenticationUtil methods.
 * <p>
 * Useful when testing using mocks.
 * 
 * @author Roy Wetherall
 * @since 2.3
 */
public class AuthenticationUtil
{
    /**
     * Helper method that executed work as system user.
     * <p>
     * Useful when testing using mocks.
     *
     * @see org.alfresco.repo.security.authentication.AuthenticationUtil#runAsSystem(RunAsWork, String)
     */
    public <R> R runAsSystem(RunAsWork<R> runAsWork)
    {
        return org.alfresco.repo.security.authentication.AuthenticationUtil.runAsSystem(runAsWork);
    }

    /**
     * Helper method that executed work as given user.
     * <p>
     * Useful when testing using mocks.
     *
     * @see org.alfresco.repo.security.authentication.AuthenticationUtil#runAs(RunAsWork, String)
     */
    public <R> R runAs(RunAsWork<R> runAsWork, String uid)
    {
        return org.alfresco.repo.security.authentication.AuthenticationUtil.runAs(runAsWork, uid);
    }
    
    /**
     * Helper method that gets the fully authenticated user.
     * <p>
     * Useful when testing using mocks.
     * 
     * @see org.alfresco.repo.security.authentication.AuthenticationUtil#getFullyAuthenticatedUser()
     */
    public String getFullyAuthenticatedUser()
    {
        return org.alfresco.repo.security.authentication.AuthenticationUtil.getFullyAuthenticatedUser();
    }
    
    /**
     * Helper method that gets the admin user name.
     * <p>
     * Useful when testing using mocks.
     * 
     * @see org.alfresco.repo.security.authentication.AuthenticationUtil#getAdminUserName()
     */
    public String getAdminUserName()
    {
        return org.alfresco.repo.security.authentication.AuthenticationUtil.getAdminUserName();
    }
    
    /**
     * Helper method that gets the system user name.
     * 
     * @see org.alfresco.repo.security.authentication.AuthenticationUtil#getSystemUserName()
     */
    public String getSystemUserName()
    {
        return org.alfresco.repo.security.authentication.AuthenticationUtil.getSystemUserName();
    }
    
    /**
     * @see org.alfresco.repo.security.authentication.AuthenticationUtil#isRunAsUserTheSystemUser()
     */
    public boolean isRunAsUserTheSystemUser()
    {
        return org.alfresco.repo.security.authentication.AuthenticationUtil.isRunAsUserTheSystemUser();        
    }
}
