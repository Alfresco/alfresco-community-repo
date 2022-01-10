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

package org.alfresco.module.org_alfresco_module_rm.security;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;

/**
 * File plan authentication service.
 *
 * @author Roy Wetherall
 * @since 2.1
 * @deprecated as of 2.2, use {@link AuthenticationUtil}.
 */
public interface FilePlanAuthenticationService
{
    /**
     * @return  rm admin user name
     *
     * @deprecated as of 2.2, use {@link AuthenticationUtil#getAdminUserName()}
     */
    String getRmAdminUserName();

    /**
     * Run provided work as the global rm admin user.
     *
     * @param <R>       return type
     * @param runAsWork work to execute as the rm admin user
     * @return R        result of work execution
     *
     * @deprecated as of 2.2, use
     *
     *
     * {@link AuthenticationUtil#runAs(RunAsWork, String)}
     */
    <R> R runAsRmAdmin(RunAsWork<R> runAsWork);
}
