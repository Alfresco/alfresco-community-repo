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
package org.alfresco.repo.security.permissions.impl;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ReflectiveMethodInvocation;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ListableBeanFactory;

import org.alfresco.repo.security.permissions.impl.acegi.MethodSecurityInterceptor;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PublicServiceAccessService;

public class PublicServiceAccessServiceImpl implements PublicServiceAccessService, BeanFactoryAware
{

    private ListableBeanFactory beanFactory;

    public AccessStatus hasAccess(String publicService, String methodName, Object... args)
    {
        Object interceptor = beanFactory.getBean(publicService + "_security");
        if (interceptor == null)
        {
            throw new UnsupportedOperationException("Unknown public service security implementation " + publicService);
        }
        if (interceptor instanceof AlwaysProceedMethodInterceptor)
        {
            return AccessStatus.ALLOWED;
        }

        if (interceptor instanceof MethodSecurityInterceptor)
        {
            MethodSecurityInterceptor msi = (MethodSecurityInterceptor) interceptor;

            MethodInvocation methodInvocation = null;
            Object publicServiceImpl = beanFactory.getBean(publicService);
            NEXT_METHOD: for (Method method : publicServiceImpl.getClass().getMethods())
            {
                if (method.getName().equals(methodName))
                {
                    if (method.getParameterTypes().length == args.length)
                    {
                        // check argument types are assignable
                        int parameterPosition = 0;
                        for (Class<?> clazz : method.getParameterTypes())
                        {
                            if (args[parameterPosition] == null)
                            {
                                if (clazz.isPrimitive())
                                {
                                    continue NEXT_METHOD;
                                }
                                else
                                {
                                    // OK, null assigns to any non-primitive type
                                }
                            }
                            else
                            {
                                if (clazz.isPrimitive())
                                {

                                    if (clazz.getName().equals("boolean"))
                                    {
                                        if (args[parameterPosition].getClass().getName().equals("java.lang.Boolean"))
                                        {
                                            // OK
                                        }
                                        else
                                        {
                                            continue NEXT_METHOD;
                                        }
                                    }
                                    else if (clazz.getName().equals("byte"))
                                    {
                                        if (args[parameterPosition].getClass().getName().equals("java.lang.Byte"))
                                        {
                                            // OK
                                        }
                                        else
                                        {
                                            continue NEXT_METHOD;
                                        }
                                    }
                                    else if (clazz.getName().equals("char"))
                                    {
                                        if (args[parameterPosition].getClass().getName().equals("java.lang.Char"))
                                        {
                                            // OK
                                        }
                                        else
                                        {
                                            continue NEXT_METHOD;
                                        }
                                    }
                                    else if (clazz.getName().equals("short"))
                                    {
                                        if (args[parameterPosition].getClass().getName().equals("java.lang.Short"))
                                        {
                                            // OK
                                        }
                                        else
                                        {
                                            continue NEXT_METHOD;
                                        }
                                    }
                                    else if (clazz.getName().equals("int"))
                                    {
                                        if (args[parameterPosition].getClass().getName().equals("java.lang.Integer"))
                                        {
                                            // OK
                                        }
                                        else
                                        {
                                            continue NEXT_METHOD;
                                        }
                                    }
                                    else if (clazz.getName().equals("long"))
                                    {
                                        if (args[parameterPosition].getClass().getName().equals("java.lang.Long"))
                                        {
                                            // OK
                                        }
                                        else
                                        {
                                            continue NEXT_METHOD;
                                        }
                                    }
                                    else if (clazz.getName().equals("float"))
                                    {
                                        if (args[parameterPosition].getClass().getName().equals("java.lang.Float"))
                                        {
                                            // OK
                                        }
                                        else
                                        {
                                            continue NEXT_METHOD;
                                        }
                                    }
                                    else if (clazz.getName().equals("double"))
                                    {
                                        if (args[parameterPosition].getClass().getName().equals("java.lang.Double"))
                                        {
                                            // OK
                                        }
                                        else
                                        {
                                            continue NEXT_METHOD;
                                        }
                                    }
                                    else
                                    {
                                        continue NEXT_METHOD;
                                    }

                                }
                                else if (!(clazz.isAssignableFrom(args[parameterPosition].getClass())))
                                {
                                    continue NEXT_METHOD;
                                }
                            }
                            parameterPosition++;
                        }
                        methodInvocation = new ReflectiveMethodInvocation(null, null, method, args, null, null) {};
                    }
                }
            }

            if (methodInvocation == null)
            {
                throw new UnsupportedOperationException("Unknown public service security implementation " + publicService + "." + methodName + " with arguments " + Arrays.toString(args));
            }

            return msi.pre(methodInvocation);
        }
        throw new UnsupportedOperationException("Unknown security interceptor " + interceptor.getClass());
    }

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException
    {
        this.beanFactory = (ListableBeanFactory) beanFactory;
    }

}
