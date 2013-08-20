/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.schedule;

import org.alfresco.repo.lock.JobLockService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * 
 * This class should be extended any time a scheduled job needs to be
 * implemented to be executed using
 * {@link org.alfresco.repo.lock.JobLockService JobLockService}. It makes the
 * cluster aware locking of the job transparent to the implementation. On the
 * job's spring {@link org.quartz.JobExecutionContext JobExecutionContext} it
 * will still always have to be passed as parameter the
 * {@link org.alfresco.repo.lock.JobLockService jobLockService}. The name to be
 * used for locking of the job is optional, if none is passed a name will be
 * composed using the simple name of the implementation class. In general if it
 * may make sense to have more than one job setup using the same class you
 * should always use a different name on each
 * {@link org.quartz.JobExecutionContext JobExecutionContext} to differentiate
 * the jobs, unless you want the lock to be shared between the different instances.
 * <p/>
 * The only method to be implemented when extending this class is {@link #executeJob(JobExecutionContext)}.
 * 
 * @author Rui Fernandes
 * @since 4.1.5
 */
public abstract class AbstractScheduledLockedJob extends QuartzJobBean
{
    private ScheduledJobLockExecuter locker;

    @Override
    protected synchronized final void executeInternal(final JobExecutionContext jobContext)
            throws JobExecutionException
    {
        if (locker == null)
        {
            JobLockService jobLockServiceBean = (JobLockService) jobContext.getJobDetail()
                    .getJobDataMap().get("jobLockService");
            if (jobLockServiceBean == null)
                throw new JobExecutionException("Missing setting for bean jobLockService");
            String name = (String) jobContext.getJobDetail().getJobDataMap().get("name");
            String jobName = name == null ? this.getClass().getSimpleName() : name;
            locker = new ScheduledJobLockExecuter(jobLockServiceBean, jobName, this);
        }
        locker.execute(jobContext);
    }

    /**
     * This is the method that should be implemented by any extension of the
     * abstract class. It won't need to worry about any lockings of the job and
     * can focus only on its specific task.
     * 
     * @param jobContext                context of the execution for retrieving services, etc
     * @throws JobExecutionException    if a job fails to execute
     */
    public abstract void executeJob(JobExecutionContext jobContext) throws JobExecutionException;
}
