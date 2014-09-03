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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * Sets runtime JVM system properties for Spring Framework. 
 * <p>
 * This class is used by the Spring framework to inject system properties into
 * the runtime environment (e.g.:  alfresco.jmx.dir).   The motivation for this 
 * is that certain values must be set within spring must be computed in advance
 * for org.springframework.beans.factory.config.PropertyPlaceholderConfigurer
 * to work properly.
 *
 * @author Jon Cox
 * @see #setProperties(String)
*/
public class RuntimeSystemPropertiesSetter implements BeanFactoryPostProcessor, ApplicationContextAware, PriorityOrdered
{
    private static Log logger = LogFactory.getLog(RuntimeSystemPropertiesSetter.class );

    /** default: just before PropertyPlaceholderConfigurer */
    private int order = Integer.MAX_VALUE - 1;
    
    private ResourcePatternResolver resolver;
    
    /**
     * @see #setProperties(String)
     */
    private Map<String, String> jvmProperties;

    public RuntimeSystemPropertiesSetter()
    {
        jvmProperties = new HashMap<String, String>(7);
    }

    /**
     * Set the properties that will get pushed into the JVM system properties.
     * This will be akin to running the JVM with the <b>-Dprop=value</b>.  Existing system JVM properties
     * <i>will not be overwritten</i>.
     * 
     * @param jvmProperties     properties to set if they are not already present in the VM
     */
    public void setJvmProperties(Map<String, String> jvmProperties)
    {
        this.jvmProperties = jvmProperties;
    }

    
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.resolver  = applicationContext;
    }

    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException 
    {
        // Push any mapped properties into the JVM
        for (Map.Entry<String, String> entry : jvmProperties.entrySet())
        {
            String key = entry.getKey();
            String value = entry.getValue();
            // Push into VM
            String currentValue = System.getProperty(key);
            if (currentValue == null)
            {
                System.setProperty(key, value);
                if (logger.isDebugEnabled())
                {
                    logger.debug("Setting system property: " + key + " = " + value);
                }
            }
        }
        
        File path=null;
        try 
        {
            // Typically, the value of 'path' will be something like:
            //
            //     $TOMCAT_HOME/webapps/alfresco/WEB-INF/classes/alfresco/alfresco-jmxrmi.password
            // or: $TOMCAT_HOME/shared/classes/alfresco/alfresco-jmxrmi.password
            path = this.resolver.getResource("classpath:alfresco/alfresco-jmxrmi.password").getFile().getCanonicalFile();
        }
        catch (Exception e ) 
        { 
            if ( logger.isWarnEnabled() )
                 logger.warn("Could not find alfresco-jmxrmi.password on classpath");
        }

        if ( path == null ) { System.setProperty("alfresco.jmx.dir", ""); }
        else
        {
            String alfresco_jmx_dir = path.getParent();

            // The value of 'alfresco.jmx.dir' will be something like:
            // $TOMCAT_HOME/webapps/alfresco/WEB-INF/classes/alfresco

            System.setProperty("alfresco.jmx.dir", alfresco_jmx_dir);
        }
    }
    public void setOrder(int order) { this.order = order; }
    public int getOrder()           { return order; }               
}
