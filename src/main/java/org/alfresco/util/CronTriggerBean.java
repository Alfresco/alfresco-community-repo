/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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

import java.util.Date;

import org.alfresco.api.AlfrescoPublicApi;     
import org.alfresco.error.AlfrescoRuntimeException;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.Trigger;

/**
 * A utility bean to wrap scheduling a cron job with a given scheduler.
 * 
 * @author Andy Hind
 */
@AlfrescoPublicApi
public class CronTriggerBean extends AbstractTriggerBean       
{
    private static final long MILLISECONDS_PER_MINUTE = 60L * 1000L;

    /*
     * Milliseconds delay before the job will be triggered.
     */
    private long startDelay = 0;
    
    /*
     * The cron expression to trigger execution.
     */
    String cronExpression;

    /**
     * Default constructor
     * 
     */
    public CronTriggerBean()
    {
        super();
    }

    /**
     * Get the cron expression that determines when this job is run.
     * 
     * @return The cron expression
     */
    public String getCronExpression()
    {
        return cronExpression;
    }

    /**
     * Set the cron expression that determines when this job is run.
     * 
     * @param cronExpression
     */
    public void setCronExpression(String cronExpression)
    {
        this.cronExpression = cronExpression;
    }

    /**
     * Build the cron trigger
     * 
     * @return The trigger
     * @throws Exception
     */
    public Trigger getTrigger() throws Exception
    {
        Trigger trigger = new CronTrigger(getBeanName(), Scheduler.DEFAULT_GROUP, getCronExpression());
        if (this.startDelay > 0)
        {
            trigger.setStartTime(new Date(System.currentTimeMillis() + this.startDelay));
        }
        JobDetail jd = super.getJobDetail();
        if (jd != null)
        {
            String jobName = super.getJobDetail().getKey().getName();
            if (jobName != null && !jobName.isEmpty())
            {
                trigger.setJobName(jobName);
            }
            String jobGroup = super.getJobDetail().getKey().getGroup();
            if (jobGroup != null && !jobGroup.isEmpty())
            {
                trigger.setJobGroup(jobGroup);
            }
        }
        return trigger;
    }
    
    public long getStartDelay()
    {
        return startDelay;
    }

    public void setStartDelay(long startDelay)
    {
        this.startDelay = startDelay;
    }

    public void setStartDelayMinutes(long startDelayMinutes)
    {
        this.startDelay = startDelayMinutes * MILLISECONDS_PER_MINUTE;
    }


    public void afterPropertiesSet() throws Exception
    {
        if ((cronExpression == null) || (cronExpression.trim().length() == 0))
        {
            throw new AlfrescoRuntimeException(
                    "The cron expression has not been set, is zero length, or is all white space");
        }
        super.afterPropertiesSet();
    }
}
