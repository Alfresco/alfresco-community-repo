/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.tenant;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.util.EqualsHelper;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.support.DelegatingIntroductionInterceptor;

/**
 * Interceptor to translate Node 
 * 
 * @since 3.0 
 * @author Derek Hulley
 * @author Jan Vonka
 */
public class MultiTNodeServiceInterceptor extends DelegatingIntroductionInterceptor//implements NodeService
{
    private static final long serialVersionUID = -5462852271914961462L;

    private static Log logger = LogFactory.getLog(MultiTNodeServiceInterceptor.class);
    
    private static ThreadLocal<Boolean> ignoreMT = new ThreadLocal<Boolean>();
    
    private TenantService tenantService;
    
    /**
     * Change the filtering behaviour of this interceptor on the current thread.
     * Use this to switch off the filtering and pass references in and out as they
     * are found.
     * 
     * @param ignoreMT      <tt>true</tt> if the current thread is able to handle
     *                      MultiTenant-enabled references. 
     * @return              <tt>true</tt> if the current transaction is MT aware
     */
    public static boolean setIgnoreMT(boolean ignoreMT)
    {
        boolean wasIgnoreMT = isIgnoreMT();
        MultiTNodeServiceInterceptor.ignoreMT.set(Boolean.valueOf(ignoreMT));
        return wasIgnoreMT;
    }
    
    /**
     * @return      Returns <tt>true</tt> if the current thread has marked itself
     *              as being able to handle MultiTenant references.
     */
    static public boolean isIgnoreMT()
    {
        if (ignoreMT.get() == null)
        {
            return false;
        }
        else
        {
            return ignoreMT.get();
        }
    }
    
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }
    
    @SuppressWarnings("unchecked")
    public Object invoke(MethodInvocation invocation) throws Throwable
    {
        // See if we can shortcut
        if (EqualsHelper.nullSafeEquals(
                AuthenticationUtil.SYSTEM_USER_NAME,
                AuthenticationUtil.getCurrentEffectiveUserName())
                ||
                !AuthenticationUtil.isMtEnabled())
        {
            return invocation.proceed();
        }

        if (logger.isDebugEnabled())
        {
            logger.debug("Intercepting method " + invocation.getMethod().getName());
        }
        
        Object[] args = invocation.getArguments();
        // Convert each of the arguments to the underlying (full) reference.
        for (int i = 0; i < args.length; i++)
        {
            Object arg = args[i];
            Object newArg = arg;
            if (arg == null)
            {
                // No conversion possible
            }
            if (arg instanceof StoreRef)
            {
                StoreRef ref = (StoreRef) arg;
                newArg = tenantService.getName(ref);
            }
            else if (arg instanceof NodeRef)
            {
                NodeRef ref = (NodeRef) arg;
                newArg = tenantService.getName(ref);
            }
            else if (arg instanceof ChildAssociationRef)
            {
                ChildAssociationRef ref = (ChildAssociationRef) arg;
                newArg = tenantService.getName(ref);
            }
            else if (arg instanceof AssociationRef)
            {
                AssociationRef ref = (AssociationRef) arg;
                newArg = tenantService.getName(ref);
            }
            
            if (logger.isDebugEnabled())
            {
                if (!EqualsHelper.nullSafeEquals(newArg, arg))
                {
                    logger.debug(
                            "Argument converted: \n" +
                            "   Before: " + arg + "\n" +
                            "   After:  " + newArg);
                }
            }
            
            // Substitute the new value
            args[i] = newArg;
        }
        
        // Make the call
        Object ret = invocation.proceed();
        
        // Convert the outbound value
        ret = convertOutboundValue(ret);
        
        // done
        return ret;
    }

    /**
     * Convert outbound collection to spoofed (no tenant prefix) values.
     */
    private Collection<Object> convertOutboundValues(Collection<Object> rawValues)
    {
        /*
         * Return types can be Lists or Sets, so cater for both.
         */
        final Collection<Object> convertedValues;
        if (rawValues instanceof List)
        {
            convertedValues = new ArrayList<Object>(rawValues.size());
        }
        else if (rawValues instanceof Set)
        {
            convertedValues = new HashSet<Object>(rawValues.size(), 1.0F);
        }
        else
        {
            throw new IllegalArgumentException("Interceptor can only handle List and Set return types.");
        }
        
        for (Object rawValue : rawValues)
        {
            Object convertedValue = convertOutboundValue(rawValue);
            convertedValues.add(convertedValue);
        }
        // Done
        return convertedValues;
    }
    
    /**
     * Convert outbound single value to spoofed (no tenant prefix) value. 
     */
    @SuppressWarnings("unchecked")
    private Object convertOutboundValue(Object rawValue)
    {
        if (rawValue == null)
        {
            return null;
        }
        // Deal with collections
        Object value = rawValue;
        if (rawValue instanceof Collection)
        {
            value = convertOutboundValues((Collection<Object>)rawValue);
        }
        else if (rawValue instanceof StoreRef)
        {
            StoreRef ref = (StoreRef) rawValue;
            value = tenantService.getName(ref);
        }
        else if (rawValue instanceof NodeRef)
        {
            NodeRef ref = (NodeRef) rawValue;
            value = tenantService.getName(ref);
        }
        else if (rawValue instanceof ChildAssociationRef)
        {
            ChildAssociationRef ref = (ChildAssociationRef) rawValue;
            value = tenantService.getName(ref);
        }
        else if (rawValue instanceof AssociationRef)
        {
            AssociationRef ref = (AssociationRef) rawValue;
            value = tenantService.getName(ref);
        }
        // Done
        return value;
    }
}
