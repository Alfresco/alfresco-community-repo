/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
