 
package org.alfresco.module.org_alfresco_module_rm.security;

/*
 * #%L
 * This file is part of Alfresco.
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
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


import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;

/**
 * @author Roy Wetherall
 * @since 2.1
 */
public class FilePlanAuthenticationServiceImpl implements FilePlanAuthenticationService
{
    /** Default rm admin user values */
    @Deprecated
    public static final String DEFAULT_RM_ADMIN_USER = "rmadmin";

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.FilePlanAuthenticationService#getRMAdminUserName()
     */
    @Override
    @Deprecated
    public String getRmAdminUserName()
    {
        return AuthenticationUtil.getAdminUserName();
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.security.FilePlanAuthenticationService#runAsRMAdmin(org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork)
     */
    @Override
    @Deprecated
    public <R> R runAsRmAdmin(RunAsWork<R> runAsWork)
    {
        return AuthenticationUtil.runAs(runAsWork, AuthenticationUtil.getAdminUserName());
    }
}
