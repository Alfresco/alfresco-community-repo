/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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
package org.alfresco.repo.admin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.properties.PropertiesConfiguration;
import org.apache.logging.log4j.core.config.properties.PropertiesConfigurationBuilder;
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
 * Alfresco modules can provide their own log4j2.properties file, which augments/overrides the global log4j2.properties
 * within the Alfresco webapp. Within the module's source tree, suppose you create:
 * <pre>
 *      config/alfresco/module/{module.id}/log4j2.properties
 * </pre>
 * At deployment time, this log4j2.properties file will be placed in:
 * <pre>
 *      WEB-INF/classes/alfresco/module/{module.id}/log4j2.properties
 * </pre>
 * Where {module.id} is whatever value is set within the AMP's module.properties file. For details, see: <a
 * href='http://wiki.alfresco.com/wiki/Developing_an_Alfresco_Module'>Developing an Alfresco Module</a>
 * <p>
 * For example, if {module.id} is "org.alfresco.module.someModule", then within your source code you'll have:
 *
 * <pre>
 * config / alfresco / module / org.alfresco.module.someModule / log4j2.properties
 * </pre>
 * <p>
 * This would be deployed to:
 * <pre>
 * WEB - INF / classes / alfresco / module / org.alfresco.module.someModule / log4j2.properties
 * </pre>
 */
public class Log4JHierarchyInit implements ApplicationContextAware
{
    private static final Log LOGGER = LogFactory.getLog(Log4JHierarchyInit.class);
    private final List<String> extraLog4jUrls;
    private ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    public Log4JHierarchyInit()
    {
        extraLog4jUrls = new ArrayList<>();
    }

    /**
     * Loads a set of augmenting/overriding log4j2.properties files from locations specified via an array of Spring URLS.
     * <p>
     * This function supports Spring's syntax for retrieving multiple class path resources with the same name,
     * via the "classpath&#042;:" prefix. For details, see: {@link PathMatchingResourcePatternResolver}.
     */
    public void setExtraLog4jUrls(List<String> urls)
    {
        extraLog4jUrls.addAll(urls);
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.resolver = applicationContext;
    }

    public void init()
    {
        importLogSettings();
    }

    private void importLogSettings()
    {
        try
        {
            Properties mainProperties = new Properties();

            importMainLogSettings(mainProperties);
            for (String url : extraLog4jUrls)
            {
                importLogSettings(url, mainProperties);
            }

            PropertiesConfiguration propertiesConfiguration = new PropertiesConfigurationBuilder()
                    .setConfigurationSource(null)
                    .setRootProperties(mainProperties)
                    .setLoggerContext((LoggerContext) LogManager.getContext(false))
                    .build();

            propertiesConfiguration.initialize();
            ((LoggerContext) LogManager.getContext(false)).reconfigure(propertiesConfiguration);
        }
        catch (Throwable t)
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Failed to add extra Logger configuration: \n" + "   Error: " + t.getMessage(), t);
            }
        }

    }

    private static void importMainLogSettings(Properties mainProperties) throws IOException
    {
        File file = ((LoggerContext) LogManager.getContext()).getConfiguration().getConfigurationSource().getFile();
        if (file != null)
        {
            try (FileInputStream fis = new FileInputStream(file))
            {
                mainProperties.load(fis);
            }
            catch (FileNotFoundException e)
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("Failed to find initial configuration: \n" + "   Error: " + e.getMessage(), e);
                }
            }
        }
    }

    private void importLogSettings(String springUrl, Properties mainProperties)
    {
        Resource[] resources = null;

        try
        {
            resources = resolver.getResources(springUrl);
        }
        catch (Exception e)
        {
            LOGGER.warn("Failed to find additional Logger configuration: " + springUrl);
        }

        // Read each resource
        for (Resource resource : resources)
        {
            try
            {
                InputStream inputStream = resource.getInputStream();
                Properties properties = new Properties();
                properties.load(inputStream);
                mainProperties.putAll(properties);
            }
            catch (Throwable e)
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("Failed to add extra Logger configuration: \n" + "   URL:   " + springUrl + "\n"
                            + "   Error: " + e.getMessage(), e);
                }
            }
        }
    }

}