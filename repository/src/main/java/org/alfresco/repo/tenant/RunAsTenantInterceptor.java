/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.tenant;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;

/**
 * @since 4.2
 */
public class RunAsTenantInterceptor implements MethodInterceptor
{
    public enum TENANT_TYPE
    {
        Default, RealUser
    }

    private TENANT_TYPE tenantType;

    public RunAsTenantInterceptor(TENANT_TYPE tenantType)
    {
        this.tenantType = tenantType;
    }

    @Override
    public Object invoke(final MethodInvocation mi) throws Throwable
    {
        TenantRunAsWork<Object> runAs = new TenantRunAsWork<Object>() {
            public Object doWork() throws Exception
            {
                try
                {
                    return mi.proceed();
                }
                catch (Throwable e)
                {
                    e.printStackTrace();

                    // Re-throw the exception
                    if (e instanceof RuntimeException)
                    {
                        throw (RuntimeException) e;
                    }
                    throw new RuntimeException("Failed to execute in RunAsTenant context", e);
                }
            }
        };

        if (tenantType == TENANT_TYPE.Default)
        {
            return TenantUtil.runAsDefaultTenant(runAs);
        }
        else
        {
            // run as tenant using current tenant context (if no tenant context then it is implied as the primary tenant, based on username)
            return TenantUtil.runAsTenant(runAs, AuthenticationUtil.getUserTenant(AuthenticationUtil.getFullyAuthenticatedUser()).getSecond());
        }
    }
}
