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
package org.alfresco.repo.audit;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.alfresco.service.Auditable;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.ProxyMethodInvocation;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ListableBeanFactory;

/**
 * Lookup the name of a bean that is being audited by {@link AuditMethodInterceptor}.
 * <p>
 * Originally used to look up public services annotated with {@code @PublicService},
 * but has now been relaxed to be any bean that uses a proxy. For the method to be
 * audited it still needs to be annotated with {@code @Auditable}.
 * 
 * @author Andy Hind, David Ward, Alan Davis
 */
public class BeanIdentifierImpl implements BeanIdentifier, BeanFactoryAware
{
    private static Log s_logger = LogFactory.getLog(BeanIdentifierImpl.class);
    private static ThreadLocal<HashMap<Method, String>> methodToBeanMap =
        new ThreadLocal<HashMap<Method, String>>();

    private ListableBeanFactory beanFactory;

    public BeanIdentifierImpl()
    {
        super();
    }

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException
    {
        this.beanFactory = (ListableBeanFactory)beanFactory;
    }

    /**
     * {@inheritDoc}
     * Cache service name look up.
     */
    public String getBeanName(MethodInvocation mi)
    {
        return getName(mi);
    }

    private String getName(MethodInvocation mi) throws BeansException
    {
        if (methodToBeanMap.get() == null)
        {
            methodToBeanMap.set(new HashMap<Method, String>());
        }
        Method method = mi.getMethod();
        String name = methodToBeanMap.get().get(method);
        if (name == null)
        {
            name = getBeanNameImpl(mi);
            methodToBeanMap.get().put(method, name);
        }
        else
        {
            if (s_logger.isDebugEnabled())
            {
                s_logger.debug("Cached look up for " + name + "." + method.getName());
            }
        }
        return name;
    }

    /**
     * Do the look up by interface type.
     * 
     * @return              Returns the name of the service or <tt>null</tt> if not found
     */
    private String getBeanNameImpl(MethodInvocation mi) throws BeansException
    {
        if (mi instanceof ProxyMethodInvocation)
        {
            Object proxy = ((ProxyMethodInvocation) mi).getProxy();
            Map beans = beanFactory.getBeansOfType(proxy.getClass());
            Iterator iter = beans.entrySet().iterator();
            while (iter.hasNext())
            {
                Map.Entry entry = (Map.Entry) iter.next();
                String name = (String) entry.getKey();
                if (proxy == entry.getValue() && !name.equals("DescriptorService"))
                {
                    return name;
                }
            }
        }
        return null;
    }
}
