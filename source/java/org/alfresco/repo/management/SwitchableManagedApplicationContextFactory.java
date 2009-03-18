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

import org.alfresco.service.Managed;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * A configurable proxy for a set of {@link ManagedApplicationContextFactory} beans that allows dynamic selection of one
 * or more alternative subsystems via {@link #setSourceBeanName}. As with other {@link ManagedBean}s, can be stopped,
 * reconfigured, started and tested. Doesn't actually implement FactoryBean because we need first class access to the
 * factory itself to be able to configure its properties.
 */
public class SwitchableManagedApplicationContextFactory implements ApplicationContextAware,
        ManagedApplicationContextFactory
{
    /** The parent application context. */
    private ApplicationContext parent;

    /** The bean name of the source {@link ManagedApplicationContextFactory}. */
    private String sourceBeanName;

    /** The current source application context factory. */
    private ManagedApplicationContextFactory sourceApplicationContextFactory;

    /**
     * Sets the bean name of the source {@link ManagedApplicationContextFactory}.
     * 
     * @param sourceBeanName
     *            the bean name
     * @throws Exception
     *             on error
     */
    @Managed(category = "management")
    public synchronized void setSourceBeanName(String sourceBeanName) throws Exception
    {
        if (this.sourceApplicationContextFactory != null)
        {
            destroy();
            this.sourceBeanName = sourceBeanName;
            onStart();
        }
        else
        {
            this.sourceBeanName = sourceBeanName;
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

    /*
     * (non-Javadoc)
     * @see org.alfresco.enterprise.repo.management.ConfigurableBean#onStart()
     */
    public synchronized void onStart()
    {
        this.sourceApplicationContextFactory = (ManagedApplicationContextFactory) this.parent
                .getBean(this.sourceBeanName);
        this.sourceApplicationContextFactory.onStart();
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.enterprise.repo.management.ConfigurableBean#onTest()
     */
    public synchronized void onTest()
    {
        if (this.sourceApplicationContextFactory != null)
        {
            this.sourceApplicationContextFactory.onTest();
        }
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.beans.factory.DisposableBean#destroy()
     */
    public synchronized void destroy() throws Exception
    {
        if (this.sourceApplicationContextFactory != null)
        {
            this.sourceApplicationContextFactory.destroy();
        }
        this.sourceApplicationContextFactory = null;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.management.ManagedApplicationContextFactory#getApplicationContext()
     */
    public synchronized ApplicationContext getApplicationContext()
    {
        if (this.sourceApplicationContextFactory == null)
        {
            onStart();
        }
        return this.sourceApplicationContextFactory.getApplicationContext();
    }
}
