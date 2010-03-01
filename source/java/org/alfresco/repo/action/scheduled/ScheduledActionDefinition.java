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
package org.alfresco.repo.action.scheduled;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.InitializingBean;

/**
 * The information needed to schedule a job.
 * 
 * The implementation is responsible for creating job details, triggers and registering with a scheduler.
 * 
 * This is not used anywhere at the moment. When we have a service then scheduled actions will be registered with the service.
 * 
 * @author Andy Hind
 */
public interface ScheduledActionDefinition extends InitializingBean
{
    /**
     * Set the template action definition that is used to build the Action to execute.
     * 
     * @param templateActionDefinition
     */
    public void setTemplateActionDefinition(TemplateActionDefinition templateActionDefinition);

    /**
     * Get the template action definition that is used to build the Action to execute.
     * 
     * @return - the template action definition.
     */
    public TemplateActionDefinition getTemplateActionDefinition();

    /**
     * Register with a scheduler. This should be called in the implementation afterPropertiesSet() method of InitializingBean
     * 
     * @param scheduler
     * @throws SchedulerException
     */
    public void register(Scheduler scheduler) throws SchedulerException;

    /**
     * Set the name of the job - used for job admin 
     * 
     * @param jobName
     */
    public void setJobName(String jobName);

    /**
     * Get the name of the job - used for job admin
     * 
     * @return - the job name
     */
    public String getJobName();

    /**
     * Set the job group - used for job admin
     * 
     * @param jobGroup
     */
    public void setJobGroup(String jobGroup);

    /**
     * Get the job group - used for job admin
     * @return - the job group.
     */
    public String getJobGroup();

    /**
     * Set the trigger name - used for job admin
     * 
     * @param triggerName
     */
    public void setTriggerName(String triggerName);

    /**
     * Get the trigger name - used for job admin
     * @return - the trigger name.
     */
    public String getTriggerName();

    /** 
     * Set the trigger group - used for job admin
     * 
     * @param triggerGroup
     */
    public void setTriggerGroup(String triggerGroup);

    /**
     * Get the trigger group - used for job admin
     * 
     * @return - the trigger group.
     */
    public String getTriggerGroup();

}
