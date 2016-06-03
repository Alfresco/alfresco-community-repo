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
