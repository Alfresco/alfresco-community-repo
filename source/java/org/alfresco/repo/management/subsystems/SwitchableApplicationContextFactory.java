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
package org.alfresco.repo.management.subsystems;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import org.springframework.context.ApplicationContext;

/**
 * A configurable proxy for a set of {@link ApplicationContextFactory} beans that allows dynamic selection of one or
 * more alternative subsystems via a <code>sourceBeanName</code> property. As with other {@link PropertyBackedBean}s,
 * can be stopped, reconfigured, started and tested.
 */
public class SwitchableApplicationContextFactory extends AbstractPropertyBackedBean implements
        ApplicationContextFactory
{

    /** The name of the property holding the bean name of the source {@link ApplicationContextFactory}. */
    private static final String SOURCE_BEAN_PROPERTY = "sourceBeanName";

    /** The default bean name of the source {@link ApplicationContextFactory}. */
    private String sourceBeanName;

    /**
     * Sets the default bean name of the source {@link ApplicationContextFactory}.
     * 
     * @param sourceBeanName
     *            the bean name
     */
    public void setSourceBeanName(String sourceBeanName)
    {
        this.sourceBeanName = sourceBeanName;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.management.subsystems.ApplicationContextFactory#getApplicationContext()
     */
    public ApplicationContext getApplicationContext()
    {
        this.lock.readLock().lock();
        try
        {
            return ((SwitchableState) getState(true)).getApplicationContext();
        }
        finally
        {
            this.lock.readLock().unlock();
        }
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.management.subsystems.AbstractPropertyBackedBean#createInitialState()
     */
    @Override
    protected PropertyBackedBeanState createInitialState() throws IOException
    {
        return new SwitchableState(this.sourceBeanName);
    }

    /**
     * Represents the state of a {@link SwitchableApplicationContextFactory}.
     */
    protected class SwitchableState implements PropertyBackedBeanState
    {

        /** The current source application context factory. */
        private ApplicationContextFactory sourceApplicationContextFactory;

        /** The bean name of the source {@link ApplicationContextFactory}. */
        private String sourceBeanName;

        /**
         * Instantiates a new switchable state.
         * 
         * @param sourceBeanName
         *            the source bean name
         */
        protected SwitchableState(String sourceBeanName)
        {
            this.sourceBeanName = sourceBeanName;
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.enterprise.repo.management.ConfigurableBean#onStart()
         */
        public void start()
        {
            if (this.sourceApplicationContextFactory == null)
            {
                this.sourceApplicationContextFactory = (ApplicationContextFactory) getParent().getBean(
                        this.sourceBeanName);
                this.sourceApplicationContextFactory.start();
            }
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.repo.management.SelfDescribingBean#onStop()
         */
        public void stop()
        {
            if (this.sourceApplicationContextFactory != null)
            {
                try
                {
                    this.sourceApplicationContextFactory.stop();
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
            this.sourceApplicationContextFactory = null;
        }

        /**
         * Gets the application context.
         * 
         * @return the application context
         */
        public ApplicationContext getApplicationContext()
        {
            if (this.sourceApplicationContextFactory == null)
            {
                start();
            }
            return this.sourceApplicationContextFactory.getApplicationContext();
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.repo.management.subsystems.PropertyBackedBean#getProperty(java.lang.String)
         */
        public String getProperty(String name)
        {
            if (!name.equals(SwitchableApplicationContextFactory.SOURCE_BEAN_PROPERTY))
            {
                return null;
            }
            return this.sourceBeanName;
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.repo.management.subsystems.PropertyBackedBean#getPropertyNames()
         */
        public Set<String> getPropertyNames()
        {
            return Collections.singleton(SwitchableApplicationContextFactory.SOURCE_BEAN_PROPERTY);
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.repo.management.subsystems.PropertyBackedBean#setProperty(java.lang.String,
         * java.lang.String)
         */
        public void setProperty(String name, String value)
        {
            if (!name.equals(SwitchableApplicationContextFactory.SOURCE_BEAN_PROPERTY))
            {
                throw new IllegalStateException("Illegal attempt to write to property \"" + name + "\"");
            }
            if (!getParent().containsBean(value))
            {
                throw new IllegalStateException("\"" + value + "\" is not a valid bean name");
            }
            if (this.sourceApplicationContextFactory != null)
            {
                stop();
                this.sourceBeanName = value;
                start();
            }
            else
            {
                this.sourceBeanName = value;
            }
        }

    }
}
