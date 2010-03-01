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
package org.alfresco.repo.admin;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * Initialises Log4j's HierarchyDynamicMBean (refer to core-services-context.xml) and any overriding log4.properties files.
 * The actual implementation uses introspection to avoid any hard-coded references to Log4J classes.  If Log4J is
 * not present, this class will do nothing.
 * <p>
 * Alfresco modules can provide their own log4j.properties file, which augments/overrides the global log4j.properties
 * within the Alfresco webapp. Within the module's source tree, suppose you create:
 * <pre>
 *      config/alfresco/module/{module.id}/log4j.properties
 * </pre>
 * At deployment time, this log4j.properties file will be placed in:
 * <pre>
 *      WEB-INF/classes/alfresco/module/{module.id}/log4j.properties
 * </pre>
 * Where {module.id} is whatever value is set within the AMP's module.properties file. For details, see: <a
 * href='http://wiki.alfresco.com/wiki/Developing_an_Alfresco_Module'>Developing an Alfresco Module</a>
 * <p>
 * For example, if {module.id} is "org.alfresco.module.avmCompare", then within your source code you'll have:
 * 
 * <pre>
 * config / alfresco / module / org.alfresco.module.avmCompare / log4j.properties
 * </pre>
 * 
 * This would be deployed to:
 * <pre>
 * WEB - INF / classes / alfresco / module / org.alfresco.module.avmCompare / log4j.properties
 * </pre>
 */
public class Log4JHierarchyInit implements ApplicationContextAware
{
    private static Log logger = LogFactory.getLog(Log4JHierarchyInit.class);
    private List<String> extraLog4jUrls;
    private ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    public Log4JHierarchyInit()
    {
        extraLog4jUrls = new ArrayList<String>();
    }

    /**
     * Loads a set of augmenting/overriding log4j.properties files from locations specified via an array of Srping URLS.
     * <p>
     * This function supports Spring's syntax for retrieving multiple class path resources with the same name,
     * via the "classpath&#042;:" prefix. For details, see: {@link PathMatchingResourcePatternResolver}.
     */
    public void setExtraLog4jUrls(List<String> urls)
    {
        for (String url : urls)
        {
            extraLog4jUrls.add(url);
        }
    }
    
    

    /* (non-Javadoc)
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.resolver = applicationContext;
    }

    @SuppressWarnings("unchecked")
    public void init()
    {
        importLogSettings();
    }

    @SuppressWarnings("unchecked")
    private void importLogSettings()
    {
        try
        {
            // Get the PropertyConfigurator
            Class clazz = Class.forName("org.apache.log4j.PropertyConfigurator");
            Method method = clazz.getMethod("configure", URL.class);
            // Import using this method
            for (String url : extraLog4jUrls)
            {
                importLogSettings(method, url);
            }
        }
        catch (ClassNotFoundException e)
        {
            // Log4J not present
            return;
        }
        catch (NoSuchMethodException e)
        {
            throw new RuntimeException("Unable to find method 'configure' on class 'org.apache.log4j.PropertyConfigurator'");
        }
        
    }

    private void importLogSettings(Method method, String springUrl)
    {
        Resource[] resources = null;

        try
        {
            resources = resolver.getResources(springUrl);
        }
        catch (Exception e)
        {
            logger.warn("Failed to find additional Logger configuration: " + springUrl);
        }

        // Read each resource
        for (Resource resource : resources)
        {
            try
            {
                URL url = resource.getURL();
                method.invoke(null, url);
            }
            catch (Throwable e)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Failed to add extra Logger configuration: \n" + "   URL:   " + springUrl + "\n" + "   Error: " + e.getMessage(), e);
                }
            }
        }
    }
}
