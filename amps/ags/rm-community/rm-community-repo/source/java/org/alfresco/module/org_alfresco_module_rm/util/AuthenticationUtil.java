/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.util;

import org.alfresco.repo.security.authentication.AuthenticationException;
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
     * @see org.alfresco.repo.security.authentication.AuthenticationUtil#runAsSystem(RunAsWork)
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
     * Helper method that gets the guest user name.
     *
     * @see org.alfresco.repo.security.authentication.AuthenticationUtil#getGuestUserName()
     */
    public String getGuestUserName()
    {
        return org.alfresco.repo.security.authentication.AuthenticationUtil.getGuestUserName();
    }

    /**
     * @see org.alfresco.repo.security.authentication.AuthenticationUtil#isRunAsUserTheSystemUser()
     */
    public boolean isRunAsUserTheSystemUser()
    {
        return org.alfresco.repo.security.authentication.AuthenticationUtil.isRunAsUserTheSystemUser();
    }

    /**
     * Helper method to get the user that is currently in effect for purposes of authentication. This includes any
     * overlays introduced by {@link #runAs}.
     *
     * @return Returns the name of the user
     * @throws AuthenticationException
     */
    public String getRunAsUser() throws AuthenticationException
    {
        return org.alfresco.repo.security.authentication.AuthenticationUtil.getRunAsUser();
    }
}
