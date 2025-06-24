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
package org.alfresco.util;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Helper class to provide static and common access to the Spring {@link org.springframework.context.ApplicationContext application context}.
 * <p>
 * <strong>Deprecated.</strong> It is better to use Spring annotations to define the application context to use in tests, see {@code org.alfresco.util.BaseSpringTest}
 * </p>
 * 
 * @author Derek Hulley
 */
@Deprecated
public class ApplicationContextHelper extends BaseApplicationContextHelper
{
    /** location of required configuration files */
    public static final String[] CONFIG_LOCATIONS = new String[]{"classpath:alfresco/application-context.xml"};

    /**
     * Provides a static, single instance of the default Alfresco application context. This method can be called repeatedly.
     * <p/>
     * If the configuration requested differs from one used previously, then the previously-created context is shut down.
     * 
     * @return Returns an application context for the default Alfresco configuration
     */
    public synchronized static ApplicationContext getApplicationContext()
    {
        return BaseApplicationContextHelper.getApplicationContext(CONFIG_LOCATIONS);
    }

    /**
     * Provides a static, single instance of an application context represented by the given array of config locations. This method can be called repeatedly.
     * <p/>
     * If the configuration requested differs from one used previously, then the previously-created context is shut down.
     * 
     * @return Returns an application context for the given config locations
     */
    public synchronized static ApplicationContext getApplicationContext(String[] configLocations)
    {
        return BaseApplicationContextHelper.getApplicationContext(configLocations);
    }

    public static void main(String... args)
    {
        ClassPathXmlApplicationContext ctx = (ClassPathXmlApplicationContext) getApplicationContext();
        synchronized (ctx)
        {
            try
            {
                ctx.wait(10000L);
            }
            catch (Throwable e)
            {}
        }
        ctx.close();
    }
}
