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
package org.alfresco.repo.management;

import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.SimpleApplicationEventMulticaster;

/**
 * A workaround for a Spring problem, where it tries to multicast to a parent application context that either hasn't
 * finished refreshing yet or is in the process of shutting down.
 * 
 * @author dward
 */
public class SafeApplicationEventMulticaster extends SimpleApplicationEventMulticaster implements
        ApplicationContextAware
{
    /** The owning application context. */
    private ApplicationContext context;

    /** Has the application started? */
    private boolean isApplicationStarted;

    /** The queued events that can't be broadcast until the application is started. */
    private List<ApplicationEvent> queuedEvents = new LinkedList<ApplicationEvent>();

    /*
     * (non-Javadoc)
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.
     * ApplicationContext)
     */
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.context = applicationContext;
        setBeanFactory(applicationContext);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.springframework.context.event.SimpleApplicationEventMulticaster#multicastEvent(org.springframework.context
     * .ApplicationEvent)
     */
    @Override
    public void multicastEvent(ApplicationEvent event)
    {
        if (event instanceof ContextRefreshedEvent && event.getSource() == this.context)
        {
            this.isApplicationStarted = true;
            for (ApplicationEvent queuedEvent : this.queuedEvents)
            {
                super.multicastEvent(queuedEvent);
            }
            this.queuedEvents.clear();
            super.multicastEvent(event);
        }
        else if (event instanceof ContextClosedEvent && event.getSource() == this.context)
        {
            this.isApplicationStarted = false;
            super.multicastEvent(event);
        }
        else if (this.isApplicationStarted)
        {
            super.multicastEvent(event);
        }
        else
        {
            this.queuedEvents.add(event);
        }
    }
}
