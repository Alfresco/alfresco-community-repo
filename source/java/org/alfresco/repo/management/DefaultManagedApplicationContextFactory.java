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
package org.alfresco.repo.management;

import java.util.Properties;

import org.alfresco.service.Managed;
import org.alfresco.util.AbstractLifecycleBean;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * A factory allowing initialisation of an entire 'subsystem' in a child application context. As with other
 * {@link ManagedBean}s, can be stopped, reconfigured, started and tested. Doesn't actually implement FactoryBean
 * because we need first class access to the factory itself to be able to configure its properties.
 */
public class DefaultManagedApplicationContextFactory extends AbstractLifecycleBean implements
        ManagedApplicationContextFactory, InitializingBean, ApplicationContextAware, BeanNameAware
{
    private static final long serialVersionUID = 6368629257690177326L;
    private ApplicationContext parent;
    private String beanName;
    private ClassPathXmlApplicationContext applicationContext;
    private Properties properties;
    private boolean autoStart;

    /*
     * (non-Javadoc)
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.
     * ApplicationContext)
     */
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.parent = applicationContext;
        super.setApplicationContext(applicationContext);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.beans.factory.BeanNameAware#setBeanName(java.lang.String)
     */
    public void setBeanName(String name)
    {
        this.beanName = name;
    }

    /**
     * @param properties
     *            the properties to set
     */
    @Managed(category = "management")
    public void setProperties(Properties properties)
    {
        this.properties = properties;
    }

    /**
     * @return the properties
     */
    public Properties getProperties()
    {
        return this.properties;
    }

    /**
     * @param autoStart
     *            should the application context be started on startup of the parent application context?
     */
    public void setAutoStart(boolean autoStart)
    {
        this.autoStart = autoStart;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception
    {
        PropertiesFactoryBean factory = new PropertiesFactoryBean();
        if (this.properties != null)
        {
            factory.setLocalOverride(true);
            factory.setProperties(this.properties);
        }
        factory.setLocations(this.parent.getResources("classpath*:alfresco/subsystems/" + this.beanName
                + "/*.properties"));
        factory.afterPropertiesSet();
        this.properties = (Properties) factory.getObject();
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.enterprise.repo.management.ConfigurableBean#onStart()
     */
    public void onStart()
    {
        this.applicationContext = new ClassPathXmlApplicationContext(new String[]
        {
            "classpath*:alfresco/subsystems/" + this.beanName + "/*-context.xml"
        }, false, this.parent);
        // Add all the post processors of the parent, e.g. to make sure system placeholders get expanded properly
        for (Object postProcessor : this.parent.getBeansOfType(BeanFactoryPostProcessor.class).values())
        {
            this.applicationContext.addBeanFactoryPostProcessor((BeanFactoryPostProcessor) postProcessor);
        }
        // Add a property placeholder configurer, with the subsystem-scoped default properties
        PropertyPlaceholderConfigurer configurer = new PropertyPlaceholderConfigurer();
        configurer.setProperties(this.properties);
        configurer.setIgnoreUnresolvablePlaceholders(true);
        this.applicationContext.addBeanFactoryPostProcessor(configurer);
        this.applicationContext.setClassLoader(parent.getClassLoader());
        this.applicationContext.refresh();
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.enterprise.repo.management.ConfigurableBean#onTest()
     */
    public void onTest()
    {
        this.applicationContext.getBean("testBean");
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.beans.factory.DisposableBean#destroy()
     */
    public void destroy() throws Exception
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
            onStart();
        }
        return this.applicationContext;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.util.AbstractLifecycleBean#onBootstrap(org.springframework.context.ApplicationEvent)
     */
    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        if (this.autoStart && this.applicationContext == null)
        {
            onStart();
        }
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.util.AbstractLifecycleBean#onShutdown(org.springframework.context.ApplicationEvent)
     */
    @Override
    protected void onShutdown(ApplicationEvent event)
    {
    }
}
