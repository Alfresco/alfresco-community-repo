/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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

import org.alfresco.repo.tenant.TenantDeployer;
import org.alfresco.repo.tenant.TenantRoutingContentStore;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.support.DefaultPointcutAdvisor;


public class CryptodocSubsystemProxyFactory extends SubsystemProxyFactory
{
    private static final long serialVersionUID = 1L;

    public CryptodocSubsystemProxyFactory()
    {
        super();
        addAdvisor(0, new DefaultPointcutAdvisor(new MethodInterceptor()
        {
            public Object invoke(MethodInvocation mi) throws Throwable
            {
                Method method = mi.getMethod();
                try
                {
                    switch (method.getName())
                    {
                        case "getTenantRoutingContentStore":
                            return getTenantRoutingContentStore(mi);
                        case "getTenantDeployer":
                            return getTenantDeployer(mi);
                        default:
                            return mi.proceed();
                    }
                }
                catch (InvocationTargetException e)
                {
                    // Unwrap invocation target exceptions
                    throw e.getTargetException();
                }
            }

            private TenantDeployer getTenantDeployer(MethodInvocation mi)
            {
                Object bean = locateBean(mi);
                if (bean instanceof TenantDeployer)
                {
                    return (TenantDeployer) bean;
                }
                return null;
            }

            private TenantRoutingContentStore getTenantRoutingContentStore(MethodInvocation mi)
            {
                Object bean = locateBean(mi);
                if (bean instanceof TenantRoutingContentStore)
                {
                    return (TenantRoutingContentStore) bean;
                }
                return null;
            }
        }));
    }
}
