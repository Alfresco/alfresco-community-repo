/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.management.subsystems;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.support.DefaultPointcutAdvisor;

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
                    if (SubsystemProxyFactory.this.sourceBeanName == null)
                    {
                        Map<?, ?> beans = SubsystemProxyFactory.this.sourceApplicationContextFactory
                                .getApplicationContext().getBeansOfType(method.getDeclaringClass());
                        if (beans.size() != 1)
                        {
                            throw new RuntimeException("Don't know where to route call to method " + method);
                        }
                        return method.invoke(beans.values().iterator().next(), mi.getArguments());
                    }
                    else
                    {
                        Object bean = SubsystemProxyFactory.this.sourceApplicationContextFactory
                                .getApplicationContext().getBean(SubsystemProxyFactory.this.sourceBeanName);
                        return method.invoke(bean, mi.getArguments());

                    }
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
}
