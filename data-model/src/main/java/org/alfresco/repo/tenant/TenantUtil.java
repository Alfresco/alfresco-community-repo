/*
 * #%L
 * Alfresco Data model classes
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

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.util.ParameterCheck;

/**
 * Utility helper methods to change the tenant context for threads.
 * 
 * @author janv
 * @author Nick Smith
 * @since 4.2
 */
public abstract class TenantUtil
{
    public static final String SYSTEM_TENANT = "-system-";
    public static final String DEFAULT_TENANT = "-default-";

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
     * @param runAsWork    the unit of work to do
     * @param uid          the user ID
     * @param tenantDomain  the tenant domain
     * @return Returns     the work's return value
     */
    public static <R> R runAsUserTenant(final TenantRunAsWork<R> runAsWork, final String uid, final String tenantDomain)
    {
        return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<R>()
        {
            public R doWork()
            {
                return runAsTenant(runAsWork, tenantDomain);
            }
        }, uid);
    }
    
    /**
     * Execute a unit of work in a given tenant context. The thread's tenant context will be returned to its normal state
     * after the call.
     * 
     * @param runAsWork    the unit of work to do
     * @param tenantDomain  the tenant domain
     * @return Returns     the work's return value
     */
    public static <R> R runAsTenant(final TenantRunAsWork<R> runAsWork, String tenantDomain)
    {
        ParameterCheck.mandatory("tenantDomain", tenantDomain);
        
        if (tenantDomain.indexOf(TenantService.SEPARATOR) > 0)
        {
            throw new AlfrescoRuntimeException("Unexpected tenant domain: "+tenantDomain+" (should not contain '"+TenantService.SEPARATOR+"')");
        }
        
        String currentTenantDomain = null;
        try
        {
            currentTenantDomain = TenantContextHolder.setTenantDomain(tenantDomain);
            return runAsWork(runAsWork);
        }
        finally
        {
            TenantContextHolder.setTenantDomain(currentTenantDomain);
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
            return runAsSystemTenant(runAsWork, TenantService.DEFAULT_DOMAIN); // force System in default domain
        }
    }
    
    // switch tenant and run as System within that tenant
    public static <R> R runAsSystemTenant(final TenantRunAsWork<R> runAsWork, final String tenantDomain)
    {
        return runAsUserTenant(runAsWork, AuthenticationUtil.getSystemUserName(), tenantDomain);
    }
    
    private static <R> R runAsWork(final TenantRunAsWork<R> runAsWork)
    {
        try
        {
            return runAsWork.doWork();
        }
        catch (Throwable e)
        {
            // Re-throw the exception
            if (e instanceof RuntimeException)
            {
                throw (RuntimeException) e;
            }
            throw new RuntimeException("Error encountered while performing TenantUtil.runAsWork: " + e.getMessage(), e);
        }
    }
    
    // note: this does not check if tenant is enabled (unlike non-static MultiTServiceImpl.getCurrentUserDomain)
    public static String getCurrentDomain()
    {
        String tenantDomain = TenantContextHolder.getTenantDomain();
        if (tenantDomain == null)
        {
            tenantDomain = TenantService.DEFAULT_DOMAIN;
        }
        return tenantDomain;
    }
    
    public static boolean isCurrentDomainDefault()
    {
        return TenantService.DEFAULT_DOMAIN.equals(getCurrentDomain());
    }
    
    public static String getTenantDomain(String name)
    {
        ParameterCheck.mandatory("name", name);
        
        int idx1 = name.indexOf(TenantService.SEPARATOR);
        if (idx1 == 0)
        {
            int idx2 = name.indexOf(TenantService.SEPARATOR, 1);
            if (idx2 != -1)
            {
                return name.substring(1, idx2);
            }
        }
        return TenantService.DEFAULT_DOMAIN;
    }

    public static boolean isDefaultTenantName(String name)
    {
        return TenantService.DEFAULT_DOMAIN.equals(getTenantDomain(name));
    }
}