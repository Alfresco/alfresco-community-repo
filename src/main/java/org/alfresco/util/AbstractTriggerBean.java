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

import org.alfresco.api.AlfrescoPublicApi;     
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.util.bean.BooleanBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.scheduling.quartz.JobDetailAwareTrigger;

/**
 * A utility bean to wrap sceduling a job with a scheduler.
 * 
 * @author Andy Hind
 */
@AlfrescoPublicApi
public abstract class AbstractTriggerBean implements InitializingBean, JobDetailAwareTrigger, BeanNameAware, DisposableBean
{

    protected static Log logger = LogFactory.getLog(AbstractTriggerBean.class);

    private JobDetail jobDetail;

    private Scheduler scheduler;

    private String beanName;
    
    private Trigger trigger;
    
    private boolean enabled = true;

    public AbstractTriggerBean()
    {
        super();
    }

    /**
     * Get the definition of the job to run.
     */
    public JobDetail getJobDetail()
    {
        return jobDetail;
    }

    /**
     * Set the definition of the job to run.
     * 
     * @param jobDetail
     */
    public void setJobDetail(JobDetail jobDetail)
    {
        this.jobDetail = jobDetail;
    }

    /**
     * Get the scheduler with which the job and trigger are scheduled.
     * 
     * @return The scheduler
     */
    public Scheduler getScheduler()
    {
        return scheduler;
    }

    /**
     * Set the scheduler.
     * 
     * @param scheduler
     */
    public void setScheduler(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }

    /**
     * Set the scheduler
     */
    public void afterPropertiesSet() throws Exception
    {
        // Check properties are set
        if (jobDetail == null)
        {
            throw new AlfrescoRuntimeException("Job detail has not been set");
        }
        if (scheduler == null)
        {
            logger.warn("Job " + getBeanName() + " is not active");
        }
        else if (!enabled)
        {
            logger.warn("Job " + getBeanName() + " is not enabled");
        }
        else
        {
            logger.info("Job " + getBeanName() + " is active and enabled");
            // Register the job with the scheduler
            this.trigger = getTrigger();
            if (this.trigger == null)
            {
                logger.error("Job " + getBeanName() + " is not active (invalid trigger)");
            }
            else
            {
                logger.info("Job " + getBeanName() + " is active");
                
                String jobName = jobDetail.getKey().getName();
                String groupName = jobDetail.getKey().getGroup();
                
                if(scheduler.getJobDetail(jobName, groupName) != null)
                {
                    // Job is already defined delete it
                    if(logger.isDebugEnabled())
                    {
                        logger.debug("job already registered with scheduler jobName:" + jobName);
                    }
                    scheduler.deleteJob(jobName, groupName);
                }
            
                if(logger.isDebugEnabled())
                {
                    logger.debug("schedule job:" + jobDetail + " using " + this.trigger
                                + " startTime: " + this.trigger.getStartTime());
                }
                scheduler.scheduleJob(jobDetail, this.trigger);
            }
        }
    }
        
    /**
     * Ensures that the job is unscheduled with the context is shut down.
     */
    public void destroy() throws Exception
    {
        if (this.trigger != null)
        {
            if (!this.scheduler.isShutdown())
            {
                scheduler.unscheduleJob(this.trigger.getName(), this.trigger.getGroup());
            }
            this.trigger = null;
        }
    }

    /**
     * Abstract method for implementations to build their trigger.
     * 
     * @return The trigger
     * @throws Exception
     */
    public abstract Trigger getTrigger() throws Exception;

    /**
     * Get the bean name as this trigger is created
     */
    public void setBeanName(String name)
    {
        this.beanName = name;
    }

    /**
     * Get the bean/trigger name.
     * 
     * @return The name of the bean
     */
    public String getBeanName()
    {
        return beanName;
    }
    
    
    public boolean isEnabled()
    {
        return enabled;
    }
    
    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public void setEnabledFromBean(BooleanBean enabled)
    {
        this.enabled = enabled.isTrue();
    }
}
