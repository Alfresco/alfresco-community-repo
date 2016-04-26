package org.alfresco.service;

import static org.junit.Assert.fail;

import java.lang.reflect.Method;

import org.alfresco.util.ApplicationContextHelper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

public class ServiceRegistryTest
{
    protected static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

    protected ServiceRegistry serviceRegistry;

    @Before
    public void before() throws Exception
    {
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
