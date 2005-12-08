/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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

import org.alfresco.service.ServiceException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * This factory provides component redirection based on Store or Node References
 * passed into the component.
 * 
 * Redirection is driven by StoreRef and NodeRef parameters. If none are given
 * in the method call, the default component is called. Otherwise, the store
 * type is extracted from these parameters and the appropriate component called
 * for the store type.
 * 
 * An error is thrown if multiple store types are found.
 * 
 * @author David Caruana
 * 
 * @param <I> The component interface class
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
     * @param binding
     *            the component to call by default
     */
    public void setDefaultBinding(I defaultBinding)
    {
        this.defaultBinding = defaultBinding;
    }

    /**
     * Sets the binding of store type (protocol string) to component
     * 
     * @param bindings
     *            the bindings
     */
    public void setRedirectedProtocolBindings(Map<String, I> protocolBindings)
    {
        this.redirectedProtocolBindings = protocolBindings;
    }

    /**
     * Sets the binding of store type (protocol string) to component
     * 
     * @param bindings
     *            the bindings
     */
    public void setRedirectedStoreBindings(Map<String, I> storeBindings)
    {
        redirectedStoreBindings = new HashMap<StoreRef, I>(storeBindings.size());
        for(String ref : storeBindings.keySet())
        {
            redirectedStoreBindings.put(new StoreRef(ref), storeBindings.get(ref));
        }
    }

    
    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws ServiceException
    {
        ParameterCheck.mandatory("Proxy Interface", proxyInterface);
        ParameterCheck.mandatory("Default Binding", defaultBinding);

        // Setup the redirector proxy
        this.redirectorProxy = (I)Proxy.newProxyInstance(proxyInterface.getClassLoader(), new Class[] { proxyInterface, StoreRedirector.class }, new RedirectorInvocationHandler());
    }

    
    /* (non-Javadoc)
     * @see org.springframework.beans.factory.FactoryBean#getObject()
     */
    public I getObject()
    {
        return redirectorProxy;
    }

    
    /* (non-Javadoc)
     * @see org.springframework.beans.factory.FactoryBean#getObjectType()
     */
    public Class getObjectType()
    {
        return proxyInterface;
    }

    
    /* (non-Javadoc)
     * @see org.springframework.beans.factory.FactoryBean#isSingleton()
     */
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
         * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
         */
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
            StoreRef storeRef = getStoreRef(args);
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
                throw e.getCause();
            }
        }
        

        /**
         * Determine store type from array of method arguments
         * 
         * @param args the method arguments
         * @return the store type (or null, if one is not specified)
         */
        private StoreRef getStoreRef(Object[] args)
        {
            StoreRef storeRef = null;

            if(args == null)
            {
                return null;
            }
            
            for (Object arg : args)
            {
                // Extract store type from argument, if store type provided
                StoreRef argStoreRef = null;
                if (arg instanceof NodeRef)
                {
                    argStoreRef = ((NodeRef) arg).getStoreRef();
                }
                else if (arg instanceof StoreRef)
                {
                    argStoreRef = ((StoreRef) arg);
                }

                // Only allow one store type
                if (argStoreRef != null)
                {
                    if (storeRef != null && !storeRef.equals(argStoreRef))
                    {
                        throw new ServiceException("Multiple store types are not supported - types " + storeRef + " and " + argStoreRef + " passed");
                    }
                    storeRef = argStoreRef;
                }
            }

            return storeRef;
        }
        

        /* (non-Javadoc)
         * @see org.alfresco.repo.service.StoreRedirector#getSupportedStoreProtocols()
         */
        public Collection<String> getSupportedStoreProtocols()
        {
            return Collections.unmodifiableCollection(StoreRedirectorProxyFactory.this.redirectedProtocolBindings.keySet());
        }

        
        /* (non-Javadoc)
         * @see org.alfresco.repo.service.StoreRedirector#getSupportedStores()
         */
        public Collection<StoreRef> getSupportedStores()
        {
            return Collections.unmodifiableCollection(StoreRedirectorProxyFactory.this.redirectedStoreBindings.keySet());
        }

    }
}
