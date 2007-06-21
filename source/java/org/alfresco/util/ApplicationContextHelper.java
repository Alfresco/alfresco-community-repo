/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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
public class ApplicationContextHelper
{
    private static ClassPathXmlApplicationContext instance;
    
    /** location of required configuration files */
    public static final String[] CONFIG_LOCATIONS = new String[] { "classpath:alfresco/application-context.xml" };
    
    /**
     * Provides a static, single instance of the application context.  This method can be
     * called repeatedly.
     * 
     * @return Returns a new application context
     */
    public synchronized static ApplicationContext getApplicationContext()
    {
        if (instance != null)
        {
            return instance;
        }
        instance = new ClassPathXmlApplicationContext(CONFIG_LOCATIONS);
        return instance;
    }
    
    /**
     * Closes and releases the application context.  On the next call to
     * {@link #getApplicationContext()}, a new context will be given.
     */
    public static synchronized void closeApplicationContext()
    {
        if (instance == null)
        {
            // Nothing to do
            return;
        }
        instance.close();
        instance = null;
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
