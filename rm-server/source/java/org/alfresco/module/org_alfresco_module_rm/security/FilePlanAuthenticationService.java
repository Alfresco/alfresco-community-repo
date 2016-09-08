/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.security;

import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;

/**
 * File plan authentication service.
 * 
 * @author Roy Wetherall
 * @since 2.1
 */
public interface FilePlanAuthenticationService
{
    /**
     * @return  rm admin user name
     */
    String getRmAdminUserName();

    /**
     * Run provided work as the global rm admin user.
     * 
     * @param <R>       return type
     * @param runAsWork work to execute as the rm admin user
     * @return R        result of work execution
     */
    <R> R runAsRmAdmin(RunAsWork<R> runAsWork);
}
