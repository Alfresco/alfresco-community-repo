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
package org.alfresco.repo.scheduler;

import org.quartz.Scheduler;
import org.quartz.core.QuartzScheduler;
import org.quartz.core.QuartzSchedulerResources;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.spi.JobStore;

/**
 * A special Scheduler Factory that provides a Scheduler reference to JobStores implementing the {@link SchedulerAware}
 * interface.
 * 
 * @author dward
 */
public class AlfrescoSchedulerFactory extends StdSchedulerFactory
{

    @Override
    protected Scheduler instantiate(QuartzSchedulerResources rsrcs, QuartzScheduler qs)
    {
        Scheduler scheduler = super.instantiate(rsrcs, qs);
        JobStore jobStore = rsrcs.getJobStore();
        if (jobStore instanceof SchedulerAware)
        {
            ((SchedulerAware) jobStore).setScheduler(scheduler);
        }
        return scheduler;
    }

}
