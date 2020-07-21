/*
 * #%L
 * Alfresco Data model classes
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

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.context.support.ServletContextResourcePatternResolver;

import sun.misc.CompoundEnumeration;

/**
 * Helper class to provide static and common access to the Spring
 * {@link org.springframework.context.ApplicationContext application context}.
 * 
 * @author Derek Hulley
 */
public abstract class BaseApplicationContextHelper
{
    private static ClassPathXmlApplicationContext instance;
    private static String[] usedConfiguration;
    private static String[] usedClassLocations;
    private static boolean useLazyLoading = false;
    private static boolean noAutoStart = false;
    
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
     * Build a classloader for the given classLocations, using the thread's context class loader as its parent.
     * 
     * @param classLocations String[]
     * @return ClassLoader
     * @throws IOException
     */
    public static ClassLoader buildClassLoader(String[] classLocations) throws IOException
    {
    	ResourceFinder resolver = new ResourceFinder();
    	// Put the test directories at the front of the classpath
    	Resource[] resources = resolver.getResources(classLocations);
    	URL[] classpath = new URL[resources.length];
    	for (int i = 0; i< resources.length; i++)
    	{
    		classpath[i] = resources[i].getURL();
    	}
    	// Let's give our classloader 'child-first' resource loading qualities!
    	ClassLoader classLoader = new URLClassLoader(classpath, Thread.currentThread().getContextClassLoader())
    	{
    		@Override
    		public URL getResource(String name)
    		{
    			URL ret = findResource(name);
    			return ret == null ? super.getResource(name) : ret;
    		}

    		@SuppressWarnings("rawtypes")
    		@Override
    		public Enumeration<URL> getResources(String name) throws IOException
    		{
    			Enumeration[] tmp = new Enumeration[2];
    			tmp[0] = findResources(name);
    			tmp[1] = super.getResources(name);
    			return new CompoundEnumeration<URL>(tmp);
    		}
    	};
    	return classLoader;
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
    public synchronized static ApplicationContext getApplicationContext(String[] configLocations, String[] classLocations) throws IOException
    {
        if (configLocations == null)
        {
            throw new IllegalArgumentException("configLocations argument is mandatory.");
        }
        if (usedConfiguration != null && Arrays.deepEquals(configLocations, usedConfiguration) && classLocations != null && Arrays.deepEquals(classLocations, usedClassLocations))
        {
            // The configuration was used to create the current context
            return instance;
        }
        // The config has changed so close the current context (if any)
        closeApplicationContext();
       
        if(useLazyLoading || noAutoStart) {
           instance = new VariableFeatureClassPathXmlApplicationContext(configLocations); 
        } else {
           instance = new ClassPathXmlApplicationContext(configLocations, false);
        }

        if(classLocations != null)
        {
        	ClassLoader classLoader = buildClassLoader(classLocations);
	        instance.setClassLoader(classLoader);
        }

        instance.refresh();

        usedConfiguration = configLocations;
        usedClassLocations = classLocations;

        return instance;
    }
    
    /**
     * Closes and releases the application context.  On the next call to
     * {@link #getApplicationContext(String[])} , a new context will be given.
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
     * Should the Spring beans be initilised in a lazy manner, or all in one go?
     * Normally lazy loading/intialising shouldn't be used when running with the
     * full context, but it may be appropriate to reduce startup times when
     * using a small, cut down context.
     */
    public static void setUseLazyLoading(boolean lazyLoading)
    {
        useLazyLoading = lazyLoading;
    }

    /**
     * Will the Spring beans be initilised in a lazy manner, or all in one go?
     * The default it to load everything in one go, as spring normally does.
     */
    public static boolean isUsingLazyLoading()
    {
        return useLazyLoading;
    }

    /**
     * Should the autoStart=true property on subsystems be honoured, or should
     * this property be ignored and the auto start prevented? Normally we will
     * use the spring configuration to decide what to start, but when running
     * tests, you can use this to prevent the auto start.
     */
    public static void setNoAutoStart(boolean noAutoStart)
    {
        BaseApplicationContextHelper.noAutoStart = noAutoStart;
    }

    /**
     * Will Subsystems with the autoStart=true property set on them be allowed
     * to auto start? The default is to honour the spring configuration and
     * allow them to, but they can be prevented if required.
     */
    public static boolean isNoAutoStart()
    {
        return noAutoStart;
    }

    /**
     * Is there currently a context loaded and cached?
     */
    public static boolean isContextLoaded()
    {
        return (instance != null);
    }

    /**
     * A wrapper around {@link ClassPathXmlApplicationContext} which allows us
     * to enable lazy loading or prevent Subsystem autostart as requested.
     */
    protected static class VariableFeatureClassPathXmlApplicationContext extends ClassPathXmlApplicationContext
    {
        protected VariableFeatureClassPathXmlApplicationContext(String[] configLocations) throws BeansException
        {
            super(configLocations);
        }

        protected void initBeanDefinitionReader(XmlBeanDefinitionReader reader)
        {
            super.initBeanDefinitionReader(reader);

            if (useLazyLoading)
            {
                LazyClassPathXmlApplicationContext.postInitBeanDefinitionReader(reader);
            }
            if (noAutoStart)
            {
                NoAutoStartClassPathXmlApplicationContext.postInitBeanDefinitionReader(reader);
            }
        }
    }

    /**
     * Can be used in Spring configuration to search for all resources matching an array of patterns.
     * 
     * @author dward
     */
    public static class ResourceFinder extends ServletContextResourcePatternResolver
    {
        public ResourceFinder()
        {
            super(new DefaultResourceLoader());
        }

        /**
         * The Constructor.
         * 
         * @param resourceLoader
         *            the resource loader
         */
        public ResourceFinder(ResourceLoader resourceLoader)
        {
            super(resourceLoader);
        }

        /**
         * Gets an array of resources matching the given location patterns.
         * 
         * @param locationPatterns
         *            the location patterns
         * @return the matching resources, ordered by locationPattern index and location in the classpath
         * @throws IOException
         *             Signals that an I/O exception has occurred.
         */
        public Resource[] getResources(String... locationPatterns) throws IOException
        {
            List<Resource> resources = new LinkedList<Resource>();
            for (String locationPattern : locationPatterns)
            {
                resources.addAll(Arrays.asList(getResources(locationPattern)));
            }
            Resource[] resourceArray = new Resource[resources.size()];
            resources.toArray(resourceArray);
            return resourceArray;
        }
    }
}
