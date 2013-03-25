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
package org.alfresco.util;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Helper class to provide static and common access to the Spring
 * {@link org.springframework.context.ApplicationContext application context}.
 * 
 * @author Derek Hulley
 */
public class ApplicationContextHelper extends BaseApplicationContextHelper
{   
    /** location of required configuration files */
    public static final String[] CONFIG_LOCATIONS = new String[] { "classpath:alfresco/application-context.xml" };
    
    /**
     * Provides a static, single instance of the default Alfresco application context.  This method can be
     * called repeatedly.
     * <p/>
     * If the configuration requested differs from one used previously, then the previously-created
     * context is shut down.
     * 
     * @return Returns an application context for the default Alfresco configuration
     */
    public synchronized static ApplicationContext getApplicationContext()
    {
        return BaseApplicationContextHelper.getApplicationContext(CONFIG_LOCATIONS);
    }
    
    public static void main(String ... args)
    {
        ClassPathXmlApplicationContext ctx = (ClassPathXmlApplicationContext) getApplicationContext();
        synchronized (ctx)
        {
            try { ctx.wait(10000L); } catch (Throwable e) {}
        }
        ctx.close();
    }
}
