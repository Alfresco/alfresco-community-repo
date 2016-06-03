package org.alfresco.repo.scheduler;

import org.quartz.Scheduler;

/**
 * An interface used by the {@link AlfrescoSchedulerFactory} in order to initialise a Quartz job store with a reference
 * to its scheduler. This is primarily to allow the monitoring interface on the job store to allow functions such as
 * 'execute now'.
 * 
 * @author dward
 */
public interface SchedulerAware
{

    /**
     * Provides a reference to the scheduler.
     * 
     * @param scheduler
     *            the scheduler
     */
    public void setScheduler(Scheduler scheduler);
}
