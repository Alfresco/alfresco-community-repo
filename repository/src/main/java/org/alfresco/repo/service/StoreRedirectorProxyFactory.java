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
package org.alfresco.repo.service;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.surf.util.ParameterCheck;

import org.alfresco.service.ServiceException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;

/**
 * This factory provides component redirection based on Store or Node References passed into the component.
 * 
 * Redirection is driven by StoreRef and NodeRef parameters. If none are given in the method call, the default component is called. Otherwise, the store type is extracted from these parameters and the appropriate component called for the store type.
 * 
 * An error is thrown if multiple store types are found.
 * 
 * @author David Caruana
 * 
 * @param <I>
 *            The component interface class
 */
public class StoreRedirectorProxyFactory<I> implements FactoryBean, InitializingBean
{
    // Logger
    private static final Log logger = LogFactory.getLog(StoreRedirectorProxyFactory.class);

    // The component interface class
    private Class<I> proxyInterface = null;

    // The default component binding
    private I defaultBinding = null;

    // The map of store types to component bindings
    private Map<String, I> redirectedProtocolBindings = null;

    // the map if more specific store Refs to component bindings
    private Map<StoreRef, I> redirectedStoreBindings = null;

    // The proxy responsible for redirection based on store type
    private I redirectorProxy = null;

    /**
     * Sets the proxy interface
     * 
     * @param proxyInterface
     *            the proxy interface
     */
    public void setProxyInterface(Class<I> proxyInterface)
    {
        this.proxyInterface = proxyInterface;
    }

    /**
     * Sets the default component binding
     * 
     * @param defaultBinding
     *            the component to call by default
     */
    public void setDefaultBinding(I defaultBinding)
    {
        this.defaultBinding = defaultBinding;
    }

    /**
     * Sets the binding of store type (protocol string) to component
     * 
     * @param protocolBindings
     *            the bindings
     */
    public void setRedirectedProtocolBindings(Map<String, I> protocolBindings)
    {
        if (protocolBindings != null)
        {
            this.redirectedProtocolBindings = Collections.unmodifiableMap(protocolBindings);
        }
        else
        {
            logger.warn("Null map injected");
            this.redirectedProtocolBindings = protocolBindings;
        }
    }

    /**
     * Sets the binding of store type (protocol string) to component
     * 
     * @param storeBindings
     *            the bindings
     */
    public void setRedirectedStoreBindings(Map<String, I> storeBindings)
    {

        Map<StoreRef, I> redirectedStoreBindingsMap = new HashMap<StoreRef, I>(storeBindings.size());
        for (String ref : storeBindings.keySet())
        {
            redirectedStoreBindingsMap.put(new StoreRef(ref), storeBindings.get(ref));
        }
        redirectedStoreBindings = Collections.unmodifiableMap(redirectedStoreBindingsMap);
    }

    /* (non-Javadoc)
     * 
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet() */
    @SuppressWarnings("unchecked")
    public void afterPropertiesSet() throws ServiceException
    {
        ParameterCheck.mandatory("Proxy Interface", proxyInterface);
        ParameterCheck.mandatory("Default Binding", defaultBinding);

        // Setup the redirector proxy
        this.redirectorProxy = (I) Proxy.newProxyInstance(proxyInterface.getClassLoader(), new Class[]{proxyInterface, StoreRedirector.class}, new RedirectorInvocationHandler());
    }

    /* (non-Javadoc)
     * 
     * @see org.springframework.beans.factory.FactoryBean#getObject() */
    public I getObject()
    {
        return redirectorProxy;
    }

    /* (non-Javadoc)
     * 
     * @see org.springframework.beans.factory.FactoryBean#getObjectType() */
    public Class getObjectType()
    {
        return proxyInterface;
    }

    /* (non-Javadoc)
     * 
     * @see org.springframework.beans.factory.FactoryBean#isSingleton() */
    public boolean isSingleton()
    {
        return true;
    }

    /**
     * Invocation handler that redirects based on store type
     */
    /* package */class RedirectorInvocationHandler implements InvocationHandler, StoreRedirector
    {

        /* (non-Javadoc)
         * 
         * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[]) */
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
        {
            // Handle StoreRedirector Interface
            if (method.getDeclaringClass().equals(StoreRedirector.class))
            {
                return method.invoke(this, args);
            }

            // Otherwise, determine the apropriate implementation to invoke for
            // the service interface method
            Object binding = null;
            StoreRef storeRef = getStoreRef(method.getParameterTypes(), args);
            if (storeRef == null)
            {
                binding = StoreRedirectorProxyFactory.this.defaultBinding;
            }
            else
            {
                if (StoreRedirectorProxyFactory.this.redirectedStoreBindings != null)
                {
                    binding = StoreRedirectorProxyFactory.this.redirectedStoreBindings.get(storeRef);
                }
                if ((binding == null) && (StoreRedirectorProxyFactory.this.redirectedProtocolBindings != null))
                {
                    binding = StoreRedirectorProxyFactory.this.redirectedProtocolBindings.get(storeRef.getProtocol());
                }
                if (binding == null)
                {
                    binding = StoreRedirectorProxyFactory.this.defaultBinding;
                }
                if (binding == null)
                {
                    throw new ServiceException("Store type " + storeRef + " is not supported");
                }
            }

            if (logger.isDebugEnabled())
                logger.debug("Redirecting method " + method + " based on store type " + storeRef);

            try
            {
                // Invoke the appropriate binding
                return method.invoke(binding, args);
            }
            catch (InvocationTargetException e)
            {
                throw e.getTargetException();
            }
        }

        /**
         * Determine store type from array of method arguments
         * 
         * @param args
         *            the method arguments
         * @return the store type (or null, if one is not specified)
         */
        private StoreRef getStoreRef(Class[] argTypes, Object[] args)
        {
            StoreRef storeRef = null;

            if (args == null)
            {
                return null;
            }

            for (int i = 0; i < argTypes.length; i++)
            {
                // Extract store type from argument, if store type provided
                StoreRef argStoreRef = null;
                if (argTypes[i].equals(NodeRef.class))
                {
                    if (args[i] != null)
                    {
                        argStoreRef = ((NodeRef) args[i]).getStoreRef();
                    }
                }
                else if (argTypes[i].equals(StoreRef.class))
                {
                    argStoreRef = ((StoreRef) args[i]);
                }

                // Only allow one store type
                if (argStoreRef != null)
                {
                    // TODO: put some thought into the ramifications of allowing cross-store moves
                    // TODO: The test here would only have checked storerefs adjacent to each other
                    // if (storeRef != null && !storeRef.equals(argStoreRef))
                    // {
                    // throw new ServiceException("Multiple store types are not supported - types " + storeRef + " and " + argStoreRef + " passed");
                    // }
                    // storeRef = argStoreRef;
                    return argStoreRef;
                }
            }

            return storeRef;
        }

        /* (non-Javadoc)
         * 
         * @see org.alfresco.repo.service.StoreRedirector#getSupportedStoreProtocols() */
        public Collection<String> getSupportedStoreProtocols()
        {
            return Collections.unmodifiableCollection(StoreRedirectorProxyFactory.this.redirectedProtocolBindings.keySet());
        }

        /* (non-Javadoc)
         * 
         * @see org.alfresco.repo.service.StoreRedirector#getSupportedStores() */
        public Collection<StoreRef> getSupportedStores()
        {
            return Collections.unmodifiableCollection(StoreRedirectorProxyFactory.this.redirectedStoreBindings.keySet());
        }

    }
}
