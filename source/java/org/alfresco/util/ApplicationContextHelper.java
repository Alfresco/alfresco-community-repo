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

import java.util.Arrays;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
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
    private static String[] usedConfiguration;
    private static boolean useLazyLoading = false;
    private static boolean noAutoStart = false;
    
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
        return getApplicationContext(CONFIG_LOCATIONS);
    }
    
    /**
     * Provides a static, single instance of the application context.  This method can be
     * called repeatedly.
     * <p/>
     * If the configuration requested differs from one used previously, then the previously-created
     * context is shut down.
     * 
     * @return Returns an application context for the given configuration
     */
    public synchronized static ApplicationContext getApplicationContext(String[] configLocations)
    {
        if (configLocations == null)
        {
            throw new IllegalArgumentException("configLocations argument is mandatory.");
        }
        if (usedConfiguration != null && Arrays.deepEquals(configLocations, usedConfiguration))
        {
            // The configuration was used to create the current context
            return instance;
        }
        // The config has changed so close the current context (if any)
        closeApplicationContext();
       
        if(useLazyLoading || noAutoStart) {
           instance = new VariableFeatureClassPathXmlApplicationContext(configLocations); 
        } else {
           instance = new ClassPathXmlApplicationContext(configLocations);
        }
                    
        usedConfiguration = configLocations;
        
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
        usedConfiguration = null;
    }
    
    /**
     * Should the Spring beans be initilised in a lazy manner, or
     *  all in one go?
     * Normally lazy loading/intialising shouldn't be used when
     *  running with the full context, but it may be appropriate
     *  to reduce startup times when using a small, cut down context.
     */
    public static void setUseLazyLoading(boolean lazyLoading) {
       useLazyLoading = lazyLoading;
    }
    /**
     * Will the Spring beans be initilised in a lazy manner, or
     *  all in one go? The default it to load everything in one
     *  go, as spring normally does.
     */
    public static boolean isUsingLazyLoading() {
       return useLazyLoading;
    }
    
    /**
     * Should the autoStart=true property on subsystems
     *  be honoured, or should this property be ignored
     *  and the auto start prevented?
     * Normally we will use the spring configuration to
     *  decide what to start, but when running tests,
     *  you can use this to prevent the auto start.
     */
    public static void setNoAutoStart(boolean noAutoStart) {
       ApplicationContextHelper.noAutoStart = noAutoStart;
    }
    /**
     * Will Subsystems with the autoStart=true property set
     *  on them be allowed to auto start? The default is to
     *  honour the spring configuration and allow them to,
     *  but they can be prevented if required.
     */
    public static boolean isNoAutoStart() {
       return noAutoStart;
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
    
   
    /**
     * A wrapper around {@link ClassPathXmlApplicationContext} which
     *  allows us to enable lazy loading or prevent Subsystem 
     *  autostart as requested.
     */
    protected static class VariableFeatureClassPathXmlApplicationContext extends ClassPathXmlApplicationContext {
       protected VariableFeatureClassPathXmlApplicationContext(String[] configLocations) throws BeansException {
          super(configLocations);
       }
       
       protected void initBeanDefinitionReader(XmlBeanDefinitionReader reader) {
          super.initBeanDefinitionReader(reader);
          
          if(useLazyLoading) {
             LazyClassPathXmlApplicationContext.postInitBeanDefinitionReader(reader);
          }
          if(noAutoStart) {
             NoAutoStartClassPathXmlApplicationContext.postInitBeanDefinitionReader(reader);
          }
       }
    }
}
