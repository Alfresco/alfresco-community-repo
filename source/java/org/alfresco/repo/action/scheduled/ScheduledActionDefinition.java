/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
     * @return
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
     * @return
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
     * @return
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
     * @return
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
     * @return
     */
    public String getTriggerGroup();

}
