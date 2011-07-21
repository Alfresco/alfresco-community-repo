/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.security.permissions.impl.acegi;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.Collection;

import net.sf.acegisecurity.Authentication;
import net.sf.acegisecurity.ConfigAttributeDefinition;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.security.permissions.PermissionCheckCollection.PermissionCheckCollectionMixin;
import org.alfresco.util.PropertyCheck;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Support to simulate interceptor-driven permissions. 
 * 
 * @author janv, Derek Hulley
 * @since 4.0
 */
public class MethodSecurityBean<R> implements InitializingBean
{
    private Log logger = LogFactory.getLog(MethodSecurityBean.class);
    
    private MethodInterceptor methodInterceptor;
    private MethodSecurityInterceptor methodSecurityInterceptor;
    private Class<?> service;
    private String methodName;
    private ConfigAttributeDefinition cad;

    /**
     * Default constructor.  Use setter methods for initialization.
     */
    public MethodSecurityBean()
    {
    }
    
    /**
     * Helper constructor to supply necessary values
     */
    public MethodSecurityBean(MethodSecurityInterceptor methodSecurityInterceptor, Class<?> service, String methodName)
    {
        this.methodSecurityInterceptor = methodSecurityInterceptor;
        this.service = service;
        this.methodName = methodName;
    }
    
    /**
     * @param methodInterceptor         an method interceptor, ideally a MethodSecurityInterceptor
     */
    public void setMethodSecurityInterceptor(MethodInterceptor methodInterceptor)
    {
        this.methodInterceptor = methodInterceptor;
    }

    public void setService(Class<?> service)
    {
        this.service = service;
    }

    public void setMethodName(String methodName)
    {
        this.methodName = methodName;
    }

    @Override
    public String toString()
    {
        return "MethodSecurityBean [serviceInterface=" + service.getName() + ", methodName=" + methodName + "]";
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory(this, "methodInterceptor", methodInterceptor);
        PropertyCheck.mandatory(this, "service", service);
        PropertyCheck.mandatory(this, "methodName", methodName);
        
        // Get the method from the service
        Method method = null;
        for (Method m : service.getMethods())
        {
            // Note: currently matches first found
            // This is fine because the interceptor requires the same defininition for all overloaded methods
            if (m.getName().equals(methodName))
            {
                method = m;
                break;
            }
        }
        
        if (method == null)
        {
            throw new AlfrescoRuntimeException(
                    "Method not found: \n" +
                    "   Interface: " + service.getClass() + "\n" +
                    "   Method:    " + methodName);
        }
        
        if (!(methodInterceptor instanceof MethodSecurityInterceptor))
        {
            // It is not an interceptor that applies security, so just ignore
            this.cad = null;
            if (logger.isTraceEnabled())
            {
                logger.trace("Method interceptor doesn't apply security: " + methodSecurityInterceptor);
            }
        }
        else
        {
            this.methodSecurityInterceptor = (MethodSecurityInterceptor) this.methodInterceptor;
            this.cad = methodSecurityInterceptor.getObjectDefinitionSource().getAttributes(new InternalMethodInvocation(method));
            // Null means there are no applicable permissions
        }
    }
    
    /**
     * @see PermissionCheckCollectionMixin#create(Collection, int, long, int)
     */
    public Collection<R> applyPermissions(
            Collection<R> toCheck,
            Authentication authentication,
            int targetResultCount)
    {
        return applyPermissions(toCheck, authentication, targetResultCount, Long.MAX_VALUE, Integer.MAX_VALUE);
    }
    
    /**
     * @see PermissionCheckCollectionMixin#create(Collection, int, long, int)
     */
    @SuppressWarnings("unchecked")
    public Collection<R> applyPermissions(
            Collection<R> toCheck,
            Authentication authentication,
            int targetResultCount, long cutOffAfterTimeMs, int cutOffAfterCount)
    {
        if (cad == null)
        {
            // Ignore permissions
            if (logger.isTraceEnabled())
            {
                logger.trace("applyPermissions ignored: " + this);
            }
            return toCheck;
        }
        // Wrap the collection to pass the information to the interceptor
        Collection<R> wrappedToCheck = PermissionCheckCollectionMixin.create(
                toCheck,
                targetResultCount, cutOffAfterTimeMs, cutOffAfterCount);
        long start = System.currentTimeMillis();
        Collection<R> ret = (Collection<R>) methodSecurityInterceptor.getAfterInvocationManager().decide(
                authentication,
                null,
                cad,
                wrappedToCheck);
        if (logger.isTraceEnabled())
        {
            logger.trace("applyPermissions: " + ret.size() + " items in " + (System.currentTimeMillis() - start) + " msecs");
        }
        return ret;
    }
    
    /**
     * Helper to provide method for permissions interceptor
     */
    class InternalMethodInvocation implements MethodInvocation 
    {
        Method method;
        
        public InternalMethodInvocation(Method method) 
        {
            this.method = method;
        }
        
        protected InternalMethodInvocation() 
        {
            throw new UnsupportedOperationException();
        }
        
        public Object[] getArguments() 
        {
            throw new UnsupportedOperationException();
        }
        
        public Method getMethod() 
        {
            return this.method;
        }
        
        public AccessibleObject getStaticPart() 
        {
            throw new UnsupportedOperationException();
        }
        
        public Object getThis() 
        {
            throw new UnsupportedOperationException();
        }
        
        public Object proceed() throws Throwable 
        {
            throw new UnsupportedOperationException();
        }
    }
}
