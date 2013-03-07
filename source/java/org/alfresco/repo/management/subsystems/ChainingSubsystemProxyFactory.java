/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
import java.util.Map;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

/**
 * A factory bean, used in conjunction with {@link ChildApplicationContextManager} allowing selected interfaces to be
 * proxied to a chain of child application contexts. To decide the target of a particular method call, the returned
 * proxy will search the application context chain in sequence and use the first one that has a bean of the required
 * name or type that doesn't implement the {@link ActivatableBean} interface or whose whose
 * {@link ActivateableBean#isActive()} method returns <code>true</code>. This allows certain functions of a chained
 * subsystem (e.g. CIFS authentication, SSO) to be targeted to specific members of the chain.
 */
public class ChainingSubsystemProxyFactory extends ProxyFactoryBean
{
    private static final long serialVersionUID = -2646392556551369220L;

    /** The source application context manager. */
    private ChildApplicationContextManager applicationContextManager;

    /** An optional bean name to look up in the source application contexts. */
    private String sourceBeanName;

    /** An optional 'fallback' object for when a suitable one could not be found in the chain *. */
    private Object defaultTarget;

    /**
     * Instantiates a new chaining subsystem proxy factory.
     */
    public ChainingSubsystemProxyFactory()
    {
        addAdvisor(new DefaultPointcutAdvisor(new MethodInterceptor()
        {
            public Object invoke(MethodInvocation mi) throws Throwable
            {
                Method method = mi.getMethod();
                try
                {
                    for (String instance : applicationContextManager.getInstanceIds())
                    {
                        ApplicationContext context;
                        try
                        {
                            context = ChainingSubsystemProxyFactory.this.applicationContextManager
                                    .getApplicationContext(instance);
                        }
                        catch (RuntimeException e)
                        {
                            // This subsystem won't start. The reason would have been logged. Ignore and continue.
                            continue;
                        }
                        if (ChainingSubsystemProxyFactory.this.sourceBeanName == null)
                        {
                            Map<?, ?> beans = context.getBeansOfType(method.getDeclaringClass());
                            Object activeBean = null;
                            for (Object bean : beans.values())
                            {
                                // Ignore inactive beans
                                if (!(bean instanceof ActivateableBean) || ((ActivateableBean) bean).isActive())
                                {
                                    if (activeBean == null)
                                    {
                                        activeBean = bean;
                                    }
                                    else
                                    {
                                        throw new RuntimeException("Don't know where to route call to method " + method
                                                + ": multiple active beans in context " + instance);
                                    }
                                }
                            }
                            if (activeBean != null)
                            {
                                return method.invoke(activeBean, mi.getArguments());
                            }
                        }
                        else
                        {
                            try
                            {
                                Object bean = context.getBean(ChainingSubsystemProxyFactory.this.sourceBeanName);

                                // Ignore inactive beans
                                if (!(bean instanceof ActivateableBean) || ((ActivateableBean) bean).isActive())
                                {
                                    return method.invoke(bean, mi.getArguments());
                                }
                            }
                            catch (NoSuchBeanDefinitionException e)
                            {
                                // Ignore and continue
                            }
                        }
                    }

                    // Fall back to the default object if we have one
                    if (defaultTarget != null && method.getDeclaringClass().isAssignableFrom(defaultTarget.getClass()))
                    {
                        return method.invoke(defaultTarget, mi.getArguments());
                    }

                    // If this is the isActive() method, we can handle it ourselves!
                    if (method.equals(ActivateableBean.class.getMethod("isActive")))
                    {
                        return Boolean.FALSE;
                    }

                    // Otherwise, something has gone wrong with wiring!
                    throw new RuntimeException("Don't know where to route call to method " + method);
                }
                catch (InvocationTargetException e)
                {
                    // Unwrap invocation target exceptions
                    throw e.getTargetException();
                }
            }
        }));
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.aop.framework.AdvisedSupport#setInterfaces(java.lang.Class[])
     */
    @SuppressWarnings("rawtypes")
    @Override
    public void setInterfaces(Class[] interfaces)
    {
        super.setInterfaces(interfaces);
        // Make it possible to export the object via JMX
        setTargetClass(getObjectType());
    }

    /**
     * Sets the application context manager.
     * 
     * @param applicationContextManager
     *            the new application context manager
     */
    public void setApplicationContextManager(ChildApplicationContextManager applicationContextManager)
    {
        this.applicationContextManager = applicationContextManager;
    }

    /**
     * Sets an optional bean name to target all calls to in the source application context. If not set, an appropriate
     * bean is looked up based on method class.
     * 
     * @param sourceBeanName
     *            the sourceBeanName to set
     */
    public void setSourceBeanName(String sourceBeanName)
    {
        this.sourceBeanName = sourceBeanName;
    }

    /**
     * Sets the default target for method calls, when a suitable target cannot be found in the application context
     * chain.
     * 
     * @param defaultTarget
     *            the defaultTarget to set
     */
    public void setDefaultTarget(Object defaultTarget)
    {
        this.defaultTarget = defaultTarget;
    }
}
