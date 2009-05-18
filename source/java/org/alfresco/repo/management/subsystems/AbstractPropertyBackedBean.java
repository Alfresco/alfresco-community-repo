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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * A base class for {@link PropertyBackedBean}s. Gets its category from its Spring bean name and automatically
 * propagates and resolves property defaults on initialization. Automatically destroys itself on server shutdown.
 * Communicates its creation and destruction to a {@link PropertyBackedBeanRegistry}.
 * 
 * @author dward
 */
public abstract class AbstractPropertyBackedBean implements PropertyBackedBean, ApplicationContextAware,
        ApplicationListener, InitializingBean, DisposableBean, BeanNameAware
{

    /** The root component of the default ID. */
    protected static final String DEFAULT_ID_ROOT = "default";

    /** The default ID (when we do not expect there to be more than one instance within a category). */
    protected static final List<String> DEFAULT_ID = Collections
            .singletonList(AbstractPropertyBackedBean.DEFAULT_ID_ROOT);

    /** The parent application context. */
    private ApplicationContext parent;

    /** The registry of all property backed beans. */
    private PropertyBackedBeanRegistry registry;

    /** The hierarchical id. Must be unique within the category. */
    private List<String> id = AbstractPropertyBackedBean.DEFAULT_ID;

    /** The category. */
    private String category;

    /** Should the application context be started on startup of the parent application?. */
    private boolean autoStart;

    /** Property defaults provided by the installer or System properties. */
    private Properties propertyDefaults;

    /** Resolves placeholders in the property defaults. */
    private DefaultResolver defaultResolver = new DefaultResolver();

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
     * Sets the registry of all property backed beans.
     * 
     * @param registry
     *            the registry of all property backed beans
     */
    public void setRegistry(PropertyBackedBeanRegistry registry)
    {
        this.registry = registry;
    }

    /**
     * Gets the registry of all property backed beans.
     * 
     * @return the registry of all property backed beans
     */
    protected PropertyBackedBeanRegistry getRegistry()
    {
        return this.registry;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.beans.factory.BeanNameAware#setBeanName(java.lang.String)
     */
    public void setBeanName(String name)
    {
        this.category = name;
    }

    /**
     * Sets the id.
     * 
     * @param id
     *            the id to set
     */
    public void setId(List<String> id)
    {
        this.id = id;
    }

    /**
     * Indicates whether the bean should be started on startup of the parent application context.
     * 
     * @param autoStart
     *            <code>true</code> if the bean should be started on startup of the parent application context
     */
    public void setAutoStart(boolean autoStart)
    {
        this.autoStart = autoStart;
    }

    /**
     * Sets the property defaults provided by the installer or System properties.
     * 
     * @param propertyDefaults
     *            the property defaults
     */
    public void setPropertyDefaults(Properties propertyDefaults)
    {
        this.propertyDefaults = propertyDefaults;
    }

    /**
     * Gets the property defaults provided by the installer or System properties.
     * 
     * @return the property defaults
     */
    protected Properties getPropertyDefaults()
    {
        return this.propertyDefaults;
    }

    /**
     * Resolves the default value of a property, if there is one, expanding placholders as necessary.
     * 
     * @param name
     *            the property name
     * @return the resolved default value or <code>null</code> if there isn't one
     */
    protected String resolveDefault(String name)
    {
        String value = this.propertyDefaults.getProperty(name);
        if (value != null)
        {
            value = this.defaultResolver.resolveValue(value);
        }
        return value == null || value.length() == 0 ? null : value;
    }

    /**
     * Gets the parent application context.
     * 
     * @return the parent application context
     */
    protected ApplicationContext getParent()
    {
        return this.parent;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception
    {
        // Override default settings using corresponding global defaults (this allows installer settings
        // to propagate through)
        for (String property : getPropertyNames())
        {
            String value = resolveDefault(property);
            if (value != null)
            {
                setProperty(property, value);
            }
        }

        this.registry.register(this);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.management.SelfDescribingBean#getId()
     */
    public List<String> getId()
    {
        return this.id;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.management.subsystems.PropertyBackedBean#getCategory()
     */
    public String getCategory()
    {
        return this.category;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.beans.factory.DisposableBean#destroy()
     */
    public void destroy()
    {
        destroy(false);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.management.subsystems.PropertyBackedBean#destroy(boolean)
     */
    public void destroy(boolean isPermanent)
    {
        stop();
        this.registry.deregister(this, isPermanent);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.management.subsystems.PropertyBackedBean#isUpdateable(java.lang.String)
     */
    public boolean isUpdateable(String name)
    {
        return true;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.management.subsystems.PropertyBackedBean#getDescription(java.lang.String)
     */
    public String getDescription(String name)
    {
        return isUpdateable(name) ? "Editable Property " + name : "Read-only Property " + name;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
     */
    public void onApplicationEvent(ApplicationEvent event)
    {
        if (this.autoStart && event instanceof ContextRefreshedEvent && event.getSource() == this.parent)
        {
            start();
        }
    }

    /**
     * Uses a Spring {@link PropertyPlaceholderConfigurer} to resolve placeholders in the property defaults. This means
     * that placeholders need not be displayed in the configuration UI or JMX console.
     */
    public class DefaultResolver extends PropertyPlaceholderConfigurer
    {

        /**
         * Instantiates a new default resolver.
         */
        public DefaultResolver()
        {
            setIgnoreUnresolvablePlaceholders(true);
        }

        /**
         * Expands the given value, resolving any ${} placeholders using the property defaults
         * 
         * @param val
         *            the value to expand
         * @return the expanded value
         */
        public String resolveValue(String val)
        {
            return AbstractPropertyBackedBean.this.propertyDefaults == null ? null : parseStringValue(val,
                    AbstractPropertyBackedBean.this.propertyDefaults, new HashSet<Object>());
        }

    }
}
