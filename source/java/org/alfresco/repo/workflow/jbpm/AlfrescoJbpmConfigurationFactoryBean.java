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
package org.alfresco.repo.workflow.jbpm;

import java.io.InputStream;

import org.hibernate.SessionFactory;
import org.jbpm.JbpmConfiguration;
import org.jbpm.JbpmContext;
import org.jbpm.configuration.ObjectFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springmodules.workflow.jbpm31.JbpmFactoryLocator;

/**
 * Implementation of Spring Module's JbpmConfigurationFactoryBean for
 * Jbpm 3.2.
 *
 * @author Costin Leau
 * @author davidc
 */
public class AlfrescoJbpmConfigurationFactoryBean implements InitializingBean, FactoryBean, BeanFactoryAware, BeanNameAware
{
    private JbpmConfiguration jbpmConfiguration;
    private ObjectFactory objectFactory;
    private Resource configuration;
    private SessionFactory sessionFactory;
    private String contextName = JbpmContext.DEFAULT_JBPM_CONTEXT_NAME;
    
    /**
     * FactoryLocator
     */
    private JbpmFactoryLocator factoryLocator = new JbpmFactoryLocator();

    
    /* (non-Javadoc)
     * @see org.springframework.beans.factory.BeanFactoryAware#setBeanFactory(org.springframework.beans.factory.BeanFactory)
     */
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException
    {
        factoryLocator.setBeanFactory(beanFactory);
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.BeanNameAware#setBeanName(java.lang.String)
     */
    public void setBeanName(String name)
    {
        factoryLocator.setBeanName(name);
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception
    {
        if (configuration == null)
            throw new IllegalArgumentException("configuration or objectFactory property need to be not null");

        // 1. Construct Jbpm Configuration
        // NOTE: Jbpm 3.2 adds a JbpmConfiguration value to its context
        InputStream stream = configuration.getInputStream();
        jbpmConfiguration = JbpmConfiguration.parseInputStream(stream);

        // 2. inject the HB session factory if it is the case
        if (sessionFactory != null)
        {
            JbpmContext context = jbpmConfiguration.createJbpmContext(contextName);
            try
            {
                context.setSessionFactory(sessionFactory);
            } finally
            {
                context.close();
            }
        }
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.FactoryBean#getObject()
     */
    public Object getObject() throws Exception
    {
        return jbpmConfiguration;
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.FactoryBean#getObjectType()
     */
    public Class getObjectType()
    {
        return JbpmConfiguration.class;
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.FactoryBean#isSingleton()
     */
    public boolean isSingleton()
    {
        return true;
    }

    /**
     * @return Returns the configuration.
     */
    public Resource getConfiguration()
    {
        return configuration;
    }

    /**
     * @param configuration  The configuration to set
     */
    public void setConfiguration(Resource configuration)
    {
        this.configuration = configuration;
    }

    /**
     * @return Returns the objectFactory.
     */
    public ObjectFactory getObjectFactory()
    {
        return objectFactory;
    }

    /**
     * @param objectFactory  The objectFactory to set
     */
    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }

    /**
     * @return Returns the contextName.
     */
    public String getContextName()
    {
        return contextName;
    }

    /**
     * @param contextName  The contextName to set
     */
    public void setContextName(String contextName)
    {
        this.contextName = contextName;
    }

    /**
     * @return Returns the sessionFactory.
     */
    public SessionFactory getSessionFactory()
    {
        return sessionFactory;
    }

    /**
     * @param sessionFactory  The sessionFactory to set
     */
    public void setSessionFactory(SessionFactory sessionFactory)
    {
        this.sessionFactory = sessionFactory;
    }

}
