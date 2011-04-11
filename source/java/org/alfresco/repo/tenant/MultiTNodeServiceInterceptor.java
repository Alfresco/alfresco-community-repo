/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.tenant;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
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
 * @author janv
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
        // See if we can shortcut (for super 'System' only)
        if (AuthenticationUtil.getSystemUserName().equals(AuthenticationUtil.getRunAsUser()) || !AuthenticationUtil.isMtEnabled())
        {
            return invocation.proceed();
        }

        String methodName = invocation.getMethod().getName();
        
        if (logger.isDebugEnabled())
        {
            logger.debug("Intercepting method " + methodName);
        }
        
        Object[] args = invocation.getArguments();
        // Convert each of the arguments to the underlying (full) reference.
        for (int i = 0; i < args.length; i++)
        {
            Object arg = args[i];
            Object newArg = convertInboundValue(arg);
            
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
        
        if (methodName.equals("getProperty"))
        {
            if (ret != null)
            {
                // Convert the outbound value
                QName qname = (QName)args[1];
                if (qname.equals(ContentModel.PROP_STORE_IDENTIFIER))
                {
                    String rawStoreId = (String)ret;
                    ret = tenantService.getBaseName(rawStoreId);
                }
                else if (qname.equals(ContentModel.PROP_CREATOR) || qname.equals(ContentModel.PROP_MODIFIER))
                {
                    // ALF-6029 (else need to patch affected spaces - eg. Models, Workflow Definitions)
                    String rawUserId = (String)ret;
                    if ("admin".equals(rawUserId))
                    {
                        ret = tenantService.getDomainUser(rawUserId, tenantService.getCurrentUserDomain());
                    }
                }
                else
                {
                    ret = convertOutboundValue(ret);
                }
            }
            
            return ret;
        }
        else if (methodName.equals("getProperties"))
        {
            if (ret != null)
            {
                // Convert the outbound values
                Map<QName, Serializable> rawValues = (Map<QName, Serializable>)ret;
                for (Map.Entry<QName, Serializable> rawValue : rawValues.entrySet())
                {
                    QName qname = rawValue.getKey();
                    Serializable value = rawValue.getValue();
                    
                    if (qname.equals(ContentModel.PROP_STORE_IDENTIFIER) && (value != null))
                    {
                        value = tenantService.getBaseName((String)value);
                    }
                    else if (qname.equals(ContentModel.PROP_CREATOR) || qname.equals(ContentModel.PROP_MODIFIER))
                    {
                        // ALF-6029 (else need to patch affected spaces - eg. Models, Workflow Definitions)
                        String rawUserId = (String)value;
                        if ("admin".equals(rawUserId))
                        {
                            value = tenantService.getDomainUser(rawUserId, tenantService.getCurrentUserDomain());
                        }
                    }
                    
                    rawValues.put(qname, (Serializable)convertOutboundValue(value));
                }
                
                ret = rawValues;
            }
            
            return ret;
        }
        else if (methodName.equals("getStores"))
        {
            if ((ret == null) || (! (ret instanceof List)))
            {
                return null;
            }
            
            List<StoreRef> rawValues = (List<StoreRef>)ret;
            final List<StoreRef> convertedValues = new ArrayList<StoreRef>(rawValues.size());
            
            for (StoreRef ref : rawValues)
            {
                StoreRef storeRef = ref;
                try
                {
                    if (tenantService.isEnabled() && (! storeRef.getProtocol().equals(StoreRef.PROTOCOL_AVM)))
                    {
                        // MT: return tenant stores only (although for super System return all stores - as used by
                        // ConfigurationChecker, IndexRecovery, IndexBackup etc)
                        if (!AuthenticationUtil.isRunAsUserTheSystemUser())
                        {
                            tenantService.checkDomain(storeRef.getIdentifier());
                            storeRef = tenantService.getBaseName(storeRef);
                        }
                    }
                    
                    convertedValues.add(storeRef);
                }
                catch (RuntimeException re)
                {
                    // deliberately ignore - stores in different domain will not be listed
                }
            }
            
            return convertedValues;
        }
        else
        {
            // Convert the outbound value
            ret = convertOutboundValue(ret);
        }
        
        // done
        return ret;
    }

    /**
     * Convert outbound collection to spoofed (ie. without tenant prefix) values.
     */
    @SuppressWarnings("unchecked")
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
     * Convert outbound single value to spoofed (ie. without tenant prefix) value.
     */
    @SuppressWarnings("unchecked")
    private Object convertOutboundValue(Object rawValue)
    {
        if (rawValue == null)
        {
            return null;
        }
        
        Object value = rawValue;
        if (rawValue instanceof Collection)
        {
            // Deal with collections
            value = convertOutboundValues((Collection<Object>)rawValue);
        }
        else if (rawValue instanceof StoreRef)
        {
            StoreRef ref = (StoreRef) rawValue;
            value = tenantService.getBaseName(ref);
        }
        else if (rawValue instanceof NodeRef)
        {
            NodeRef ref = (NodeRef) rawValue;
            value = tenantService.getBaseName(ref);
        }
        else if (rawValue instanceof ChildAssociationRef)
        {
            ChildAssociationRef ref = (ChildAssociationRef) rawValue;
            value = tenantService.getBaseName(ref);
        }
        else if (rawValue instanceof AssociationRef)
        {
            AssociationRef ref = (AssociationRef) rawValue;
            value = tenantService.getBaseName(ref);
        }
        else if (rawValue instanceof Path)
        {
            Path ref = (Path)rawValue;
            Path outboundPath = new Path();
            Iterator<Path.Element> itr = ref.iterator();
            while (itr.hasNext())
            {
                Path.Element pathElement = itr.next();
                if (pathElement instanceof Path.ChildAssocElement)
                {
                    pathElement = new Path.ChildAssocElement(tenantService.getBaseName(((Path.ChildAssocElement)pathElement).getRef()));
                }
                outboundPath.append(pathElement);
            }
            value = outboundPath;
        }
        // Done
        return value;
    }
    
    /**
     * Convert inbound collection to non-spoofed (ie. with tenant prefix) values.
     */
    @SuppressWarnings("unchecked")
    private Collection<Object> convertInboundValues(Collection<Object> rawValues)
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
            Object convertedValue = convertInboundValue(rawValue);
            convertedValues.add(convertedValue);
        }
        // Done
        return convertedValues;
    }
    
    /**
     * Convert outbound single value to non-spoofed (ie. with tenant prefix) value.
     */
    @SuppressWarnings("unchecked")
    private Object convertInboundValue(Object rawValue)
    {
        if (rawValue == null)
        {
            return null;
        }

        Object value = rawValue;
        if (rawValue instanceof StoreRef)
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
        else if (rawValue instanceof Collection)
        {
            // Deal with collections
            value = convertInboundValues((Collection<Object>)rawValue);
        }
        else if (rawValue instanceof Path)
        {
            Path ref = (Path)rawValue;
            Path inboundPath = new Path();
            Iterator<Path.Element> itr = ref.iterator();
            while (itr.hasNext())
            {
                Path.Element pathElement = itr.next();
                if (pathElement instanceof Path.ChildAssocElement)
                {
                    pathElement = new Path.ChildAssocElement(tenantService.getName(((Path.ChildAssocElement)pathElement).getRef()));
                }
                inboundPath.append(pathElement);
            }
            value = inboundPath;
        }
        // Done
        return value;
    }
}
