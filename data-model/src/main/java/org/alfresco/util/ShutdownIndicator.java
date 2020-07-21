/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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
package org.alfresco.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextClosedEvent;

/**
 * Listens to ApplicationEvents to provide a simple {@link #isShuttingDown()} method, so callers don't need to be
 * Spring beans or listen to these events themselves. Intended for use by code that wishes to avoid ERROR log messages
 * on shutdown.
 *
 * @author adavis
 */
public class ShutdownIndicator implements ApplicationContextAware, ApplicationListener<ApplicationContextEvent>
{
    private boolean shuttingDown;
    private ApplicationContext applicationContext;

    public synchronized boolean isShuttingDown()
    {
        return shuttingDown;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(ApplicationContextEvent event)
    {
        if (event instanceof ContextClosedEvent && event.getSource() == applicationContext)
        {
            synchronized (this)
            {
                shuttingDown = true;
            }
        }
    }
}
