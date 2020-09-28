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
package org.alfresco.util;

import org.alfresco.error.AlfrescoRuntimeException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

public class SchedulerStarterBean extends AbstractLifecycleBean
{
    protected final static Log log = LogFactory.getLog(SchedulerStarterBean.class);    
    
    private Scheduler scheduler;

    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        try
        {
            log.info("Scheduler started");
            scheduler.start();
        }
        catch (SchedulerException e)
        {
            throw new AlfrescoRuntimeException("Scheduler failed to start", e);
        }
    }

    @Override
    protected void onShutdown(ApplicationEvent event)
    {
        // Nothing required
        // This is done by the SchedulerFactoryBean.destroy() - DisposableBean 
    }

    public Scheduler getScheduler()
    {
        return scheduler;
    }

    public void setScheduler(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }

}
