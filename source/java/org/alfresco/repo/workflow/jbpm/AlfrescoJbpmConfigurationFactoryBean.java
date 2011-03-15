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
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springmodules.workflow.jbpm31.JbpmFactoryLocator;

/**
 * Implementation of Spring Module's JbpmConfigurationFactoryBean for Jbpm 3.2.
 * 
 * @author Costin Leau
 * @author davidc
 */
public class AlfrescoJbpmConfigurationFactoryBean implements InitializingBean, FactoryBean<JbpmConfiguration>,
            BeanFactoryAware, BeanNameAware, DisposableBean
{
    private JbpmConfiguration jbpmConfiguration;

    private ObjectFactory objectFactory;

    private Resource configuration;

    private SessionFactory sessionFactory;

    private String contextName = JbpmContext.DEFAULT_JBPM_CONTEXT_NAME;

    /**
     * FactoryLocator
     */
    private final AlfrescoJbpmFactoryLocator factoryLocator = new AlfrescoJbpmFactoryLocator();

    /*
     * (non-Javadoc)
     * @see
     * org.springframework.beans.factory.BeanFactoryAware#setBeanFactory(org
     * .springframework.beans.factory.BeanFactory)
     */
    public void setBeanFactory(final BeanFactory beanFactory) throws BeansException
    {
        // TODO Added to get the build working. A better solution is needed
        // long-term.
        this.factoryLocator.destroy();

        this.factoryLocator.setBeanFactory(beanFactory);
    }

    /**
    * {@inheritDoc}
     */
    public void setBeanName(final String name)
    {
        this.factoryLocator.setBeanName(name);
    }

    /**
    * {@inheritDoc}
     */
    public void afterPropertiesSet() throws Exception
    {
        if (this.configuration == null) { throw new IllegalArgumentException(
                    "configuration or objectFactory property need to be not null"); }

        // 1. Construct Jbpm Configuration
        // NOTE: Jbpm 3.2 adds a JbpmConfiguration value to its context
        final InputStream stream = this.configuration.getInputStream();
        this.jbpmConfiguration = JbpmConfiguration.parseInputStream(stream);

        // 2. inject the HB session factory if it is the case
        if (this.sessionFactory != null)
        {
            final JbpmContext context = this.jbpmConfiguration.createJbpmContext(this.contextName);
            try
            {
                context.setSessionFactory(this.sessionFactory);
            }
            finally
            {
                context.close();
            }
        }
    }

    /**
     * {@inheritDoc}
      */
    public JbpmConfiguration getObject() throws Exception
    {
        return this.jbpmConfiguration;
    }

    /**
     * {@inheritDoc}
      */
    public Class<JbpmConfiguration> getObjectType()
    {
        return JbpmConfiguration.class;
    }

    /**
     * {@inheritDoc}
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
        return this.configuration;
    }

    /**
     * @param configuration The configuration to set
     */
    public void setConfiguration(final Resource configuration)
    {
        this.configuration = configuration;
    }

    /**
     * @return Returns the objectFactory.
     */
    public ObjectFactory getObjectFactory()
    {
        return this.objectFactory;
    }

    /**
     * @param objectFactory The objectFactory to set
     */
    public void setObjectFactory(final ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }

    /**
     * @return Returns the contextName.
     */
    public String getContextName()
    {
        return this.contextName;
    }

    /**
     * @param contextName The contextName to set
     */
    public void setContextName(final String contextName)
    {
        this.contextName = contextName;
    }

    /**
     * @return Returns the sessionFactory.
     */
    public SessionFactory getSessionFactory()
    {
        return this.sessionFactory;
    }

    /**
     * @param sessionFactory The sessionFactory to set
     */
    public void setSessionFactory(final SessionFactory sessionFactory)
    {
        this.sessionFactory = sessionFactory;
    }

    /**
     * {@inheritDoc}
      */
    public void destroy() throws Exception
    {
        this.factoryLocator.destroy();
    }

    private static class AlfrescoJbpmFactoryLocator extends JbpmFactoryLocator
    {
        public void destroy()
        {
            JbpmFactoryLocator.beanFactories.clear();
            JbpmFactoryLocator.beanFactoriesNames.clear();
            JbpmFactoryLocator.referenceCounter.clear();
            JbpmFactoryLocator.canUseDefaultBeanFactory = true;
            JbpmFactoryLocator.defaultFactory = null;
        }
    }

}
