/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.management;

import java.util.LinkedList;
import java.util.List;

import org.springframework.extensions.surf.util.AbstractLifecycleBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;

/**
 * An event publisher that is safe to use while the context is in the process of refreshing. It queues up events until
 * the context has refreshed, after which point events are published in real time.
 * 
 * @author dward
 */
public class SafeEventPublisher extends AbstractLifecycleBean implements ApplicationEventPublisher
{

    /** Has the application started? */
    private boolean isApplicationStarted;

    /** The queued events. */
    private List<ApplicationEvent> queuedEvents = new LinkedList<ApplicationEvent>();

    /*
     * (non-Javadoc)
     * @see org.springframework.extensions.surf.util.AbstractLifecycleBean#onBootstrap(org.springframework.context.ApplicationEvent)
     */
    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        this.isApplicationStarted = true;
        for (ApplicationEvent queuedEvent : this.queuedEvents)
        {
            publishEvent(queuedEvent);
        }
        this.queuedEvents.clear();
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.extensions.surf.util.AbstractLifecycleBean#onShutdown(org.springframework.context.ApplicationEvent)
     */
    @Override
    protected void onShutdown(ApplicationEvent event)
    {
        this.isApplicationStarted = false;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.springframework.context.ApplicationEventPublisher#publishEvent(org.springframework.context.ApplicationEvent)
     */
    public void publishEvent(ApplicationEvent event)
    {
        ApplicationContext context = getApplicationContext();
        if (this.isApplicationStarted)
        {
            context.publishEvent(event);
        }
        else
        {
            this.queuedEvents.add(event);
        }
    }

}
