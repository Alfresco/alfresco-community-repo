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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Thread local to maintain tenant context for threads.
 * 
 * @author janv
 * @since Thor
 */
public class TenantContextHolder
{
    private static Log logger = LogFactory.getLog(TenantContextHolder.class);
    
    private static final ThreadLocal<String> contextHolder = new ThreadLocal<String>();
    
    public static String setTenantDomain(String tenantDomain)
    {
        String currentTenantDomain = getTenantDomain();
        
        if (tenantDomain == null)
        {
            clearTenantDomain();
            return currentTenantDomain;
        }
        
        // force lower-case
        tenantDomain = tenantDomain.toLowerCase();
        
        if (tenantDomain.equals(currentTenantDomain))
        {
            return currentTenantDomain;
        }
        
        contextHolder.set(tenantDomain);
        
        if (logger.isTraceEnabled())
        {
            logger.trace("Set tenant: "+tenantDomain);
        }
        
        return currentTenantDomain;
    }
    
    public static String getTenantDomain() 
    {
        return (String)contextHolder.get();
    }
    
    public static void clearTenantDomain() 
    {
        if (logger.isTraceEnabled())
        {
            String tenantDomain = getTenantDomain();
            if (! TenantService.DEFAULT_DOMAIN.equals(tenantDomain))
            {
                logger.trace("Clear tenant domain (was: "+getTenantDomain()+")");
            }
        }
        
        contextHolder.remove();
    }
}
