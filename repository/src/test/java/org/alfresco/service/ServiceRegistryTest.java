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
package org.alfresco.service;

import static org.junit.Assert.fail;

import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import org.alfresco.util.ApplicationContextHelper;

public class ServiceRegistryTest
{
    protected ApplicationContext ctx;

    protected ServiceRegistry serviceRegistry;

    @Before
    public void before() throws Exception
    {
        ctx = ApplicationContextHelper.getApplicationContext();
        serviceRegistry = (ServiceRegistry) ctx.getBean("ServiceRegistry");
    }

    @Test
    public void testServiceRegistryGetters() throws Exception
    {
        Method[] methods = serviceRegistry.getClass().getMethods();
        for (Method method : methods)
        {
            if (method.getName().startsWith("get") && (method.getParameterTypes().length == 0))
            {
                try
                {
                    method.invoke(serviceRegistry, null);
                }
                catch (java.lang.reflect.InvocationTargetException i)
                {
                    if (i.getTargetException() instanceof UnsupportedOperationException)
                    {
                        continue;
                    }
                    fail("Failed to invoke " + method.getName() + " : " + i.getTargetException().getMessage());
                }
                catch (Exception e)
                {
                    fail("Failed to invoke " + method.getName() + " : " + e.getMessage());
                }

            }
        }
    }
}
