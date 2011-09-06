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
package org.alfresco.repo.management.subsystems;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.context.ApplicationContext;

/**
 * A factory bean, normally used in conjunction with {@link ChildApplicationContextFactory} allowing selected
 * interfaces in a child application context to be proxied by a bean in the parent application context. This allows
 * 'hot-swapping' and reconfiguration of entire subsystems.
 */
public class SubsystemProxyFactory extends ProxyFactoryBean
{
    private static final long serialVersionUID = -4186421942840611218L;
    
    /** The source application context factory. */
    private ApplicationContextFactory sourceApplicationContextFactory;
    
    /** An optional bean name to look up in the source application context **/
    private String sourceBeanName;

    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private ApplicationContext context;
    private Object sourceBean;
    private Map <Class<?>, Object> typedBeans = new HashMap<Class<?>, Object>(7);

    /**
     * Instantiates a new managed subsystem proxy factory.
     */
    public SubsystemProxyFactory()
    {
        addAdvisor(new DefaultPointcutAdvisor(new MethodInterceptor()
        {
            public Object invoke(MethodInvocation mi) throws Throwable
            {
                Method method = mi.getMethod();
                try
                {
                    return method.invoke(locateBean(mi), mi.getArguments());
                }
                catch (InvocationTargetException e)
                {
                    // Unwrap invocation target exceptions
                    throw e.getTargetException();
                }
            }
        }));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setInterfaces(Class[] interfaces)
    {
        super.setInterfaces(interfaces);
        // Make it possible to export the object via JMX
        setTargetClass(getObjectType());
    }


    /**
     * Sets the source application context factory.
     * 
     * @param sourceApplicationContextFactory
     *            the sourceApplicationContextFactory to set
     */
    public void setSourceApplicationContextFactory(ApplicationContextFactory sourceApplicationContextFactory)
    {
        this.sourceApplicationContextFactory = sourceApplicationContextFactory;
    }

    /**
     * Sets optional bean name to target all calls to in the source application context. If not set, an appropriate
     * bean is looked up based on method class.
     * 
     * @param sourceBeanName
     *            the sourceBeanName to set
     */
    public void setSourceBeanName(String sourceBeanName)
    {
        this.sourceBeanName = sourceBeanName;
    }
    
    // Bring our cached copies of the source beans in line with the application context factory, using a RW lock to
    // ensure consistency
    private Object locateBean(MethodInvocation mi)
    {
        boolean haveWriteLock = false;
        this.lock.readLock().lock();
        try
        {
            ApplicationContext newContext = this.sourceApplicationContextFactory.getApplicationContext();
            if (this.context != newContext)
            {
                // Upgrade the lock
                this.lock.readLock().unlock();
                this.lock.writeLock().lock();
                haveWriteLock = true;

                newContext = this.sourceApplicationContextFactory.getApplicationContext();
                this.context = newContext;
                this.typedBeans.clear();
                this.sourceBean = null;
                if (this.sourceBeanName != null)
                {
                    this.sourceBean = newContext.getBean(this.sourceBeanName);
                }
            }
            if (this.sourceBean == null)
            {
                Method method = mi.getMethod();                
                Class<?> type = method.getDeclaringClass();
                Object bean = this.typedBeans.get(type);
                if (bean == null)
                {
                    // Upgrade the lock if necessary
                    if (!haveWriteLock)
                    {
                        this.lock.readLock().unlock();
                        this.lock.writeLock().lock();
                        haveWriteLock = true;
                    }
                    bean = this.typedBeans.get(type);
                    if (bean == null)
                    {
                        Map<?, ?> beans = this.context.getBeansOfType(type);
                        if (beans.size() != 1)
                        {
                            throw new RuntimeException("Don't know where to route call to method " + method);
                        }
                        bean = beans.values().iterator().next();
                        this.typedBeans.put(type, bean);
                    }
                }
                return bean;
            }
            return this.sourceBean;
        }
        finally
        {
            if (haveWriteLock)
            {
                this.lock.writeLock().unlock();
            }
            else
            {
                this.lock.readLock().unlock();
            }
        }
    }
    
}
