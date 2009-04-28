/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.management.subsystems;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.alfresco.config.JBossEnabledResourcePatternResolver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * A factory allowing initialization of an entire 'subsystem' in a child application context. As with other
 * {@link PropertyBackedBean}s, can be stopped, reconfigured, started and tested. Each instance possesses a
 * <code>typeName</code> property, that determines where the factory will look for default configuration for the
 * application context. In addition, its <code>id</code> property will determine where the factory will look for
 * override configuration in the extension classpath.
 * <p>
 * The factory will search for a Spring application context in the classpath using the following patterns in order:
 * <ul>
 * <li>alfresco/subsystems/&lt;category>/&lt;typeName>/*-context.xml</li>
 * <li>alfresco/extension/subsystems/&lt;category>/&lt;typeName>/&lt;id/*-context.xml</li>
 * </ul>
 * The child application context may use <code>${}</code> placeholders, and will be configured with a
 * {@link PropertyPlaceholderConfigurer} initialised with properties files found in classpath matching the following
 * patterns in order:
 * <ul>
 * <li>alfresco/subsystems/&lt;category>/&lt;typeName>/*.properties</li>
 * <li>alfresco/extension/subsystems/&lt;category>/&lt;typeName>/&lt;id/*.properties</li>
 * </ul>
 * This means that the extension classpath can be used to provide instance-specific overrides to product default
 * settings. Of course, if you are using the Enterprise edition, you might want to use a JMX client such as JConsole to
 * edit the settings instead!
 */
public class ChildApplicationContextFactory extends AbstractPropertyBackedBean implements ApplicationContextAware,
        ApplicationListener, ApplicationContextFactory
{

    /** The name of the special read-only property containing the type name. */
    private static final String TYPE_NAME_PROPERTY = "$type";

    /** The Constant PROPERTIES_SUFFIX. */
    private static final String PROPERTIES_SUFFIX = "/*.properties";

    /** The Constant CONTEXT_SUFFIX. */
    private static final String CONTEXT_SUFFIX = "/*-context.xml";

    /** The Constant CLASSPATH_PREFIX. */
    private static final String CLASSPATH_PREFIX = "classpath*:alfresco/subsystems/";

    /** The Constant EXTENSION_CLASSPATH_PREFIX. */
    private static final String EXTENSION_CLASSPATH_PREFIX = "classpath*:alfresco/extension/subsystems/";

    /** The logger. */
    private static Log logger = LogFactory.getLog(ChildApplicationContextFactory.class);

    /** The parent. */
    private ApplicationContext parent;

    /** The properties. */
    private Properties properties;

    /** The application context. */
    private ClassPathXmlApplicationContext applicationContext;

    /** The auto start. */
    private boolean autoStart;

    /** The type name. */
    private String typeName;

    /**
     * Default constructor for container construction.
     */
    protected ChildApplicationContextFactory()
    {
    }

    /**
     * Constructor for dynamically created instances, e.g. through {@link DefaultChildApplicationContextManager}.
     * 
     * @param parent
     *            the parent application context
     * @param registry
     *            the registry of property backed beans
     * @param category
     *            the category
     * @param typeName
     *            the type name
     * @param id
     *            the instance id
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public ChildApplicationContextFactory(ApplicationContext parent, PropertyBackedBeanRegistry registry,
            String category, String typeName, String id) throws IOException
    {
        setApplicationContext(parent);
        setRegistry(registry);
        setBeanName(category);
        setTypeName(typeName);
        setId(id);

        try
        {
            afterPropertiesSet();
        }
        catch (RuntimeException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.
     * ApplicationContext)
     */
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.parent = applicationContext;
    }

    /**
     * Indicates whether the application context be started on startup of the parent application context.
     * 
     * @param autoStart
     *            <code>true</code> if the application context should be started on startup of the parent application
     *            context
     */
    public void setAutoStart(boolean autoStart)
    {
        this.autoStart = autoStart;
    }

    /**
     * Sets the type name.
     * 
     * @param typeName
     *            the typeName to set
     */
    public void setTypeName(String typeName)
    {
        this.typeName = typeName;
    }

    /**
     * Gets the type name.
     * 
     * @return the type name
     */
    public String getTypeName()
    {
        return this.typeName;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception
    {
        if (getTypeName() == null)
        {
            setTypeName(getId());
        }

        // Load the property defaults
        PropertiesFactoryBean factory = new PropertiesFactoryBean();

        Resource[] baseResources = this.parent.getResources(ChildApplicationContextFactory.CLASSPATH_PREFIX
                + getCategory() + '/' + getTypeName() + ChildApplicationContextFactory.PROPERTIES_SUFFIX);
        // Allow overrides from the extension classpath
        Resource[] extensionResources = this.parent
                .getResources(ChildApplicationContextFactory.EXTENSION_CLASSPATH_PREFIX + getCategory() + '/'
                        + getTypeName() + '/' + getId() + '/' + ChildApplicationContextFactory.PROPERTIES_SUFFIX);
        Resource[] combinedResources;
        if (baseResources.length == 0)
        {
            combinedResources = extensionResources;
        }
        else if (extensionResources.length == 0)
        {
            combinedResources = baseResources;
        }
        else
        {
            combinedResources = new Resource[baseResources.length + extensionResources.length];
            System.arraycopy(baseResources, 0, combinedResources, 0, baseResources.length);
            System.arraycopy(extensionResources, 0, combinedResources, baseResources.length, extensionResources.length);
        }
        factory.setLocations(combinedResources);
        factory.afterPropertiesSet();
        this.properties = (Properties) factory.getObject();

        super.afterPropertiesSet();
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.management.subsystems.PropertyBackedBean#getPropertyNames()
     */
    @SuppressWarnings("unchecked")
    public synchronized Set<String> getPropertyNames()
    {
        Set<String> result = new TreeSet<String>(((Map) this.properties).keySet());
        result.add(ChildApplicationContextFactory.TYPE_NAME_PROPERTY);
        return result;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.management.subsystems.PropertyBackedBean#getProperty(java.lang.String)
     */
    public synchronized String getProperty(String name)
    {
        if (name.equals(ChildApplicationContextFactory.TYPE_NAME_PROPERTY))
        {
            return getTypeName();
        }
        else
        {
            return this.properties.getProperty(name);
        }

    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.management.subsystems.PropertyBackedBean#setProperty(java.lang.String, java.lang.String)
     */
    public void setProperty(String name, String value)
    {
        if (name.equals(ChildApplicationContextFactory.TYPE_NAME_PROPERTY))
        {
            throw new IllegalStateException("Illegal write to property \""
                    + ChildApplicationContextFactory.TYPE_NAME_PROPERTY + "\"");
        }
        if (value == null)
        {
            this.properties.remove(name);
        }
        else
        {
            this.properties.setProperty(name, value);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.management.subsystems.AbstractPropertyBackedBean#isUpdateable(java.lang.String)
     */
    @Override
    public boolean isUpdateable(String name)
    {
        // Only the type property is read only
        return !name.equals(ChildApplicationContextFactory.TYPE_NAME_PROPERTY);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.management.subsystems.PropertyBackedBean#start()
     */
    public synchronized void start()
    {
        this.applicationContext = new ClassPathXmlApplicationContext(new String[]
        {
            ChildApplicationContextFactory.CLASSPATH_PREFIX + getCategory() + '/' + getTypeName()
                    + ChildApplicationContextFactory.CONTEXT_SUFFIX,
            ChildApplicationContextFactory.EXTENSION_CLASSPATH_PREFIX + getCategory() + '/' + getTypeName() + '/'
                    + getId() + '/' + ChildApplicationContextFactory.CONTEXT_SUFFIX
        }, false, this.parent)
        {

            /*
             * (non-Javadoc)
             * @see org.springframework.context.support.AbstractApplicationContext#getResourcePatternResolver()
             */
            @Override
            protected ResourcePatternResolver getResourcePatternResolver()
            {
                return new JBossEnabledResourcePatternResolver(this);
            }

        };

        // Add a property placeholder configurer, with the subsystem-scoped default properties
        PropertyPlaceholderConfigurer configurer = new PropertyPlaceholderConfigurer();
        configurer.setProperties(this.properties);
        configurer.setIgnoreUnresolvablePlaceholders(true);
        this.applicationContext.addBeanFactoryPostProcessor(configurer);

        // Add all the post processors of the parent, e.g. to make sure system placeholders get expanded properly
        for (Object postProcessor : this.parent.getBeansOfType(BeanFactoryPostProcessor.class).values())
        {
            this.applicationContext.addBeanFactoryPostProcessor((BeanFactoryPostProcessor) postProcessor);
        }

        this.applicationContext.setClassLoader(this.parent.getClassLoader());
        this.applicationContext.refresh();
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.management.subsystems.PropertyBackedBean#stop()
     */
    public void stop()
    {
        if (this.applicationContext != null)
        {
            this.applicationContext.close();
            this.applicationContext = null;
        }
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.management.ManagedApplicationContextFactory#getApplicationContext()
     */
    public synchronized ApplicationContext getApplicationContext()
    {
        if (this.applicationContext == null)
        {
            start();
        }
        return this.applicationContext;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
     */
    public void onApplicationEvent(ApplicationEvent event)
    {
        // Automatically refresh the application context on boot up, if required
        if (this.autoStart && event instanceof ContextRefreshedEvent && event.getSource() == this.parent)
        {
            getApplicationContext();
        }
    }
}
