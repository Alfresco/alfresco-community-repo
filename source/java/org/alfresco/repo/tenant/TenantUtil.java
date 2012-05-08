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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.tenant;

import org.alfresco.repo.security.authentication.AuthenticationUtil;

/**
 * Utility helper methods to change the tenant context for threads.
 *
 * @since Thor
 */
public class TenantUtil
{
    public interface TenantRunAsWork<Result>
    {
        /**
         * Method containing the work to be done
         * 
         * @return Return the result of the operation
         */
        Result doWork() throws Exception;
    }

    /**
     * Execute a unit of work in a given tenant context. The thread's tenant context will be returned to its normal state
     * after the call.
     * 
     * @param runAsWork  the unit of work to do
     * @param uid        the user ID
     * @return Returns the work's return value
     */
    public static <R> R runAsPrimaryTenant(final TenantRunAsWork<R> runAsWork, String user)
    {
        // TODO need to differentiate between
        // - tenant user - with implied context (in MT Ent world)
        // - system users as a tenant
        // - super tenant only
        // etc

        // TODO for now, this is just a brute force change of tenant regardless of above
        //      scenarios
        String runAsUser = AuthenticationUtil.getRunAsUser();
        if (runAsUser == null || runAsUser.equals(user))
        {
            return runAsWork(runAsWork);
        }
        else
        {
            return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<R>()
            {
                public R doWork()
                {
                    return runAsWork(runAsWork);
                }
            }, user);
        }
    }
    
    /**
     * Execute a unit of work in a given tenant context. The thread's tenant context will be returned to its normal state
     * after the call.
     * 
     * @param runAsWork  the unit of work to do
     * @param uid        the user ID
     * @return Returns the work's return value
     */
    public static <R> R runAsTenant(final TenantRunAsWork<R> runAsWork, String tenantDomain)
    {
        // TODO need to differentiate between
        // - tenant user - with implied context (in MT Ent world)
        // - system users as a tenant
        // - super tenant only
        // etc

        // TODO for now, this is just a brute force change of tenant regardless of above
        //      scenarios
        
        if (getCurrentDomain().equals(tenantDomain))
        {
            return runAsWork(runAsWork);
        }
        else
        {
            return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<R>()
            {
                public R doWork()
                {
                    return runAsWork(runAsWork);
                }
            }, AuthenticationUtil.getRunAsUser() + TenantService.SEPARATOR + tenantDomain);
        }
    }

    public static <R> R runAsDefaultTenant(final TenantRunAsWork<R> runAsWork)
    {
        // Note: with MT Enterprise, if you're current user is not already part of the default domain then this will switch to System
        if (getCurrentDomain().equals(TenantService.DEFAULT_DOMAIN))
        {
            return runAsWork(runAsWork);
        }
        else
        {
            return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<R>()
            {
                public R doWork()
                {
                    return runAsWork(runAsWork);
                }
            }, AuthenticationUtil.getSystemUserName() + TenantService.SEPARATOR); // force default domain;
        }
    }
    
    // switch tenant and run as System within that tenant
    public static <R> R runAsSystemTenant(final TenantRunAsWork<R> runAsWork, final String tenantDomain)
    {
        return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<R>()
        {
            public R doWork()
            {
                return runAsWork(runAsWork);
            }
        }, AuthenticationUtil.getSystemUserName() + TenantService.SEPARATOR + tenantDomain);
    }
    
    private static <R> R runAsWork(final TenantRunAsWork<R> runAsWork)
    {
        try
        {
            return runAsWork.doWork();
        }
        catch (Throwable exception)
        {
            // Re-throw the exception
            if (exception instanceof RuntimeException)
            {
                throw (RuntimeException) exception;
            }
            throw new RuntimeException("Error during run as.", exception);
        }
    }
    
    // note: this does not check if tenant is enabled (unlike non-static MultiTServiceImpl.getCurrentUserDomain)
    public static String getCurrentDomain()
    {
        if (AuthenticationUtil.isMtEnabled())
        {
            String runAsUser = AuthenticationUtil.getRunAsUser();
            if (runAsUser != null)
            {
                int idx = runAsUser.lastIndexOf(TenantService.SEPARATOR);
                if ((idx > 0) && (idx < (runAsUser.length()-1)))
                {
                    return runAsUser.substring(idx+1);
                }
            }
        }
        return TenantService.DEFAULT_DOMAIN;
    }
}